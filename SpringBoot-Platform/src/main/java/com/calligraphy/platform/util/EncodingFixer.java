import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class EncodingFixer {
    public static void main(String[] args) throws IOException {
        Path sourceDir = Paths.get("src/main/java");

        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(EncodingFixer::fixEncoding);
        }
    }

    private static void fixEncoding(Path file) {
        try {
            // Read file content with UTF-8 encoding
            String content = Files.readString(file, StandardCharsets.UTF_8);

            // Write back with UTF-8 encoding to ensure proper encoding
            Files.writeString(file, content, StandardCharsets.UTF_8);

            System.out.println("Fixed encoding for: " + file);
        } catch (IOException e) {
            System.err.println("Error processing file: " + file + ": " + e.getMessage());
        }
    }
}
