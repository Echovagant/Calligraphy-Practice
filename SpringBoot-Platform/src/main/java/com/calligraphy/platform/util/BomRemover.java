import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BomRemover {
    public static void main(String[] args) throws IOException {
        Path sourceDir = Paths.get("src/main/java");
        
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(BomRemover::removeBom);
        }
    }
    
    private static void removeBom(Path file) {
        try {
            // Read the file content as bytes to check for BOM
            byte[] contentBytes = Files.readAllBytes(file);
            
            // Check if the file has a BOM (EF BB BF)
            if (contentBytes.length >= 3 && contentBytes[0] == (byte) 0xEF && contentBytes[1] == (byte) 0xBB && contentBytes[2] == (byte) 0xBF) {
                // Remove the BOM by skipping the first 3 bytes
                byte[] newContent = new byte[contentBytes.length - 3];
                System.arraycopy(contentBytes, 3, newContent, 0, newContent.length);
                
                // Write back the content without BOM
                Files.write(file, newContent);
                System.out.println("Removed BOM from: " + file);
            }
        } catch (IOException e) {
            System.err.println("Error processing file: " + file + ": " + e.getMessage());
        }
    }
}