import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class AnnotationCleanup {
    public static void main(String[] args) throws IOException {
        // 移除所有@NonNull注解但保留代码结构
        Path sourceDir = Paths.get("src/main/java");

        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(AnnotationCleanup::cleanFile);
        }
    }

    private static void cleanFile(Path file) {
        try {
            String content = Files.readString(file);

            // 移除所有@NonNull注解（各种变体）
            content = content
                    .replace("@NonNull", "")
                    .replace("@Nonnull", "")
                    .replace("@NotNull", "")
                    .replace("import org.springframework.lang.NonNull;", "")
                    .replace("import javax.annotation.Nonnull;", "")
                    .replace("import jakarta.annotation.Nonnull;", "")
                    .replace("import lombok.NonNull;", "")
                    .replace("import javax.annotation.Nullable;", "import org.springframework.lang.Nullable;")
                    .replace("import jakarta.annotation.Nullable;", "import org.springframework.lang.Nullable;");

            // 修复多余的空格
            content = content
                    .replaceAll("\\s+@Override", "\n    @Override")
                    .replaceAll("\n\\s*\n\\s*\n", "\n\n");

            Files.writeString(file, content);
        } catch (IOException e) {
            System.err.println("Error cleaning file: " + file);
        }
    }
}