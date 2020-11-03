package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * Creates a file or a directory with a given path.
     * If the directories in the path do not exist, they will be created.
     *
     * @param filePath
     * @param isDirectory
     * @return the created file
     */
    public static File createFileOrDirectory(String filePath, boolean isDirectory) {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }

        try {
            if (isDirectory) {
                file = new File(filePath);
                file.mkdirs();
            } else {
                Files.createDirectories(Paths.get(file.getParent()));
                file = new File(filePath);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
