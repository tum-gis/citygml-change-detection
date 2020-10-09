package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * Creates a file with a given path. If the directories do not exist, they will be created.
     *
     * @param filePath
     * @return the created file
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }

        try {
            Files.createDirectories(Paths.get(file.getParent()));
            file = new File(filePath);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
