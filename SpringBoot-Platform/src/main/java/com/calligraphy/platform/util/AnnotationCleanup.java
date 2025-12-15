import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class AnnotationCleanup {
    public static void main(String[] args) throws IOException {
        // 绉婚櫎鎵€鏈堾NonNull娉ㄨВ浣嗕繚鐣欎唬鐮佺粨鏋?        Path sourceDir = Paths.get("src/main/java");

        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(AnnotationCleanup::cleanFile);
        }
    }

    private static void cleanFile(Path file) {
        try {
            String content = Files.readString(file);

            // 绉婚櫎鎵€鏈堾NonNull娉ㄨВ锛堝悇绉嶅彉浣擄級
            content = content
                    .replace("", "")
                    .replace("", "")
                    .replace("", "")
                    .replace("", "")
                    .replace("", "")
                    .replace("", "")
                    .replace("", "")
                    .replace("import org.springframework.lang.Nullable;", "import org.springframework.lang.Nullable;")
                    .replace("import org.springframework.lang.Nullable;", "import org.springframework.lang.Nullable;");

            // 淇PostConstruct瀵煎叆
            content = content
                    .replace("import jakarta.annotation.PostConstruct;", "import jakarta.annotation.PostConstruct;")
                    .replace("import jakarta.annotation.PreDestroy;", "import jakarta.annotation.PreDestroy;")
                    .replace("jakarta.annotation.PostConstruct", "jakarta.annotation.PostConstruct")
                    .replace("jakarta.annotation.PreDestroy", "jakarta.annotation.PreDestroy");

            // 淇澶氫綑鐨勭┖鏍?            content = content
                    .replaceAll("\\s+@Override", "\n    @Override")
                    .replaceAll("\n\\s*\n\\s*\n", "\n\n");

            Files.writeString(file, content);
        } catch (IOException e) {
            System.err.println("Error cleaning file: " + file);
        }
    }
}

