package utils;

import java.nio.file.Files;
import java.nio.file.Path;

/** Tim thu muc chua src va web de co the chay tu IntelliJ/VS Code. */
public class ProjectPaths {
    private static boolean isProject(Path folder) {
        return folder != null
            && Files.isRegularFile(folder.resolve("web/index.html"))
            && Files.isDirectory(folder.resolve("src/main"));
    }

    public static Path root() {
        // Tim tu thu muc chay hien tai, cha cua no va thu muc con.
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int level = 0; level < 8 && current != null; level++) {
            if (isProject(current)) return current;
            if (isProject(current.resolve("DatDoAnKTX_Web_Java_DA_SUA_LOI")))
                return current.resolve("DatDoAnKTX_Web_Java_DA_SUA_LOI");
            if (isProject(current.resolve("DatDoAnKTX_Web_Java")))
                return current.resolve("DatDoAnKTX_Web_Java");
            current = current.getParent();
        }
        // Neu class chay tu out/production cua IntelliJ, tim theo vi tri class.
        try {
            Path classes = Path.of(ProjectPaths.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toAbsolutePath();
            current = classes;
            for (int level = 0; level < 8 && current != null; level++) {
                if (isProject(current)) return current;
                if (isProject(current.resolve("DatDoAnKTX_Web_Java_DA_SUA_LOI")))
                    return current.resolve("DatDoAnKTX_Web_Java_DA_SUA_LOI");
                current = current.getParent();
            }
        } catch (Exception ignored) { }
        throw new IllegalStateException(
            "Khong thay thu muc web/index.html. Hay mo thu muc chua ca src va web.");
    }
}
