package com.thoughtworks.cruise.tlb.utils;

import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class FileUtil {

    public static File createTempFolder() {
        final File file = new File(tempFolder(), UUID.randomUUID().toString());
        file.mkdirs();
        file.deleteOnExit();
        return file;
    }

    private static String tempFolder() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File createFileInFolder(File folder, String fileName) {
        File file = new File(folder, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        file.deleteOnExit();
        return file;
    }
}
