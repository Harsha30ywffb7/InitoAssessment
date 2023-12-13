import java.util.*;

public class InMemoryFileSystem {
    private Directory root;
    private Directory currentDirectory;

    public InMemoryFileSystem() {
        root = new Directory("/");
        currentDirectory = root;
    }

    public void mkdir(String directoryName) {
        Directory newDirectory = new Directory(directoryName);
        currentDirectory.directories.add(newDirectory);
    }

    public void cd(String path) {
        if (path.equals("/")) {
            currentDirectory = root;
        } else if (path.equals("..")) {
            if (currentDirectory != root) {
                currentDirectory = getParentDirectory(currentDirectory, root);
            }
        } else if (path.startsWith("/")) {
            currentDirectory = getDirectoryByPath(root, path.substring(1));
        } else {
            currentDirectory = getDirectoryByPath(currentDirectory, path);
        }
    }

    private Directory getParentDirectory(Directory current, Directory root) {
        if (current == root) {
            return root;
        }
        for (Directory directory : root.directories) {
            if (directory.directories.contains(current)) {
                Directory result = getParentDirectory(current, directory);
                if (result != null) {
                    return result;
                }

            } else {
                return root;
            }
        }
        return null;
    }

    private Directory getDirectoryByPath(Directory current, String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.equals("..")) {
                current = getParentDirectory(current, root);
            } else {
                boolean found = false;
                for (Directory directory : current.directories) {
                    if (directory.name.equals(part)) {
                        current = directory;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Directory not found
                    return current;
                }
            }
        }
        return current;
    }

    public void ls(String path) {
        Directory targetDirectory = path.equals("") ? currentDirectory : getDirectoryByPath(currentDirectory, path);
        if (targetDirectory != null) {
            System.out.println("Contents of " + targetDirectory.name + ":");
            for (Directory directory : targetDirectory.directories) {
                System.out.println(directory.name + "/");
            }
            for (File file : targetDirectory.files) {
                System.out.println(file.name);
            }
        } else {
            System.out.println("Directory not found");
        }
    }

    public void touch(String fileName) {
        File newFile = new File(fileName, "");
        currentDirectory.files.add(newFile);
    }

    public void echo(String fileName, String content) {
        File file = getFileByName(currentDirectory, fileName);
        if (file != null) {
            file.content = content;
        } else {
            System.out.println("File not found");
        }
    }

    private File getFileByName(Directory current, String fileName) {
        for (File file : current.files) {
            if (file.name.equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    public void cat(String fileName) {
        File file = getFileByName(currentDirectory, fileName);
        if (file != null) {
            System.out.println(file.content);
        } else {
            System.out.println("File not found");
        }
    }

   public void cp(String sourcePath, String destinationPath) {
    Directory sourceDirectory = getDirectoryByPath(currentDirectory, sourcePath);
    Directory destinationDirectory = getDirectoryByPath(currentDirectory, destinationPath);

    if (sourceDirectory != null && destinationDirectory != null) {
        // Copy files
        List<File> filesToCopy = new ArrayList<>(sourceDirectory.files);
        for (File file : filesToCopy) {
            File newFile = new File(file.name, file.content);
            destinationDirectory.files.add(newFile);
        }

        // Recursively copy directories
        List<Directory> directoriesToCopy = new ArrayList<>(sourceDirectory.directories);
        for (Directory directory : directoriesToCopy) {
            copyDirectory(directory, destinationDirectory);
        }
    } else {
        System.out.println("Invalid source or destination path");
    }
}



    private void copyDirectory(Directory source, Directory destinationParent) {
        Directory newDirectory = new Directory(source.name);
        destinationParent.directories.add(newDirectory);

        // Copy files
        for (File file : source.files) {
            File newFile = new File(file.name, file.content);
            newDirectory.files.add(newFile);
        }

        // Recursively copy directories
        for (Directory directory : source.directories) {
            copyDirectory(directory, newDirectory);
        }
    }

    public void mv(String sourcePath, String destinationPath) {
        cp(sourcePath, destinationPath);
        rm(sourcePath);
    }

    public void rm(String path) {
        Directory targetDirectory = getDirectoryByPath(currentDirectory, path);

        if (targetDirectory != null) {
            // Remove files
            targetDirectory.files.clear();

            // Recursively remove directories
            for (Directory directory : targetDirectory.directories) {
                removeDirectory(directory, targetDirectory);
            }
            targetDirectory.directories.clear();
        } else {
            File file = getFileByName(currentDirectory, path);
            if (file != null) {
                currentDirectory.files.remove(file);
            } else {
                System.out.println("File or directory not found");
            }
        }
    }

    private void removeDirectory(Directory directory, Directory parentDirectory) {
        // Recursively remove directories
        for (Directory subdirectory : directory.directories) {
            removeDirectory(subdirectory, directory);
        }

        // Remove files
        directory.files.clear();

        // Remove the directory from the parent directory
        parentDirectory.directories.remove(directory);
    }

    public String getPath() {
    return getPathHelper(root, currentDirectory);
}

// Helper method to recursively build the path
private String getPathHelper(Directory root, Directory current) {
    if (current == root) {
        return root.name;
    } else {
        return getPathHelper(root, getParentDirectory(current, root)) + "/" + current.name;
    }
}

    public static void main(String[] args) {
        InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(fileSystem.getPath() + "> ");
            String command = scanner.nextLine();
            String[] parts = command.split("\\s+");
            String[] contentArray = command.split("\\s*>\\s*", 2);

            switch (parts[0]) {
                case "mkdir":
                    fileSystem.mkdir(parts[1]);
                    break;
                case "cd":
                    fileSystem.cd(parts[1]);
                    break;
                case "ls":
                    fileSystem.ls(parts.length > 1 ? parts[1] : "");
                    break;
                case "cat":
                    fileSystem.cat(parts[1]);
                    break;
                case "touch":
                    fileSystem.touch(parts[1]);
                    break;
                case "echo":
                     String content = contentArray[0].substring(6,contentArray[0].length()-1);
                     String filename = contentArray[1];
                    fileSystem.echo(filename, content);
                    break;
                case "cp":
                    fileSystem.cp(parts[1], parts[2]);
                    break;
                case "mv":
                    fileSystem.mv(parts[1], parts[2]);
                    break;
                case "rm":
                    fileSystem.rm(parts[1]);
                    break;
                case "exit":
                    System.out.println("Exiting the file system");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }
    }
}
