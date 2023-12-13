import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String command = "echo 'I am finding difficulty in write this to file'> file.txt";

        System.out.println();
        System.out.println();
         String[] contentArray = command.split("\\s*>\\s*", 2);
        String content = contentArray[0].substring(6,contentArray[0].length()-1);
        String filename = contentArray[1];
        System.out.println(content);
        System.out.println(filename);

    }
}
