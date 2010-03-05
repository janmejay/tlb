package com.thoughtworks.cruise.tlb.utils;

import static com.thoughtworks.cruise.tlb.TlbConstants.TLB_TMP_DIR;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    private SystemEnvironment env;
    public static final String TMP_DIR = "java.io.tmpdir";

    public FileUtil(SystemEnvironment env) {
        this.env = env;
    }



    String tempFolder() {
        String overriddenTmpDir = env.getProperty(TLB_TMP_DIR);
        return overriddenTmpDir == null ? System.getProperty(TMP_DIR) : overriddenTmpDir;
    }

    public File createFileInFolder(File folder, String fileName) {
        File file = new File(folder, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        file.deleteOnExit();
        return file;
    }

    public String classFileRelativePath(String testClass) {
        return testClass.replaceAll("\\.", "/") + ".class";
    }

    public File getUniqueFile(String seedString) {
        String fileName = DigestUtils.md5Hex(seedString);
        return new File(new File(tempFolder()), fileName);
    }
}
