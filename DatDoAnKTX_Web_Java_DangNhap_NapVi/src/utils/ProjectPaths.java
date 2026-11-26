package utils;

import java.nio.file.*;

/** Tu tim thu muc chua src va web, du mo project tu IntelliJ hay VS Code. */
public class ProjectPaths {
    private static boolean isProject(Path folder) {
        return folder != null && Files.isRegularFile(folder.resolve("web/index.html"))
                && Files.isDirectory(folder.resolve("src/main"));
    }
    private static Path checkFolderAndChildren(Path folder) {
        if (folder == null) return null;
        if (isProject(folder)) return folder;
        if (!Files.isDirectory(folder)) return null;
        try (DirectoryStream<Path> children = Files.newDirectoryStream(folder)) {
            for (Path child : children) if (Files.isDirectory(child) && isProject(child)) return child;
        } catch (Exception ignored) { /* Thu vi tri khac */ }
        return null;
    }
    private static Path searchUp(Path start) {
        Path current = start;
        for (int i = 0; i < 7 && current != null; i++) {
            Path found = checkFolderAndChildren(current);
            if (found != null) return found;
            current = current.getParent();
        }
        return null;
    }
    public static Path root() {
        Path found = searchUp(Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize());
        if (found != null) return found;
        try {
            Path location = Path.of(ProjectPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            found = searchUp(location);
            if (found != null) return found;
        } catch (Exception ignored) { }
        throw new IllegalStateException("Không tìm thấy project. Hãy chạy thử mục có src và web.");
    }
}
