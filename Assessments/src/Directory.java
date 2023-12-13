import java.util.ArrayList;
import java.util.List;

class Directory {
    String name;
    List<Directory> directories;
    List<File> files;

    public Directory(String name) {
        this.name = name;
        this.directories = new ArrayList<>();
        this.files = new ArrayList<>();
    }
}
