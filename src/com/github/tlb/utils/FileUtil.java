package com.github.tlb.utils;

import static com.github.tlb.TlbConstants.TLB_TMP_DIR;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileUtil {
    private SystemEnvironment env;
    public static final String TMP_DIR = "java.io.tmpdir";
    public static final Logger logger = Logger.getLogger(FileUtil.class.getName());

    public FileUtil(SystemEnvironment env) {
        this.env = env;
    }

    public String tmpDir() {
        String tmpDir = env.getProperty(TLB_TMP_DIR);
        if (tmpDir == null) {
            tmpDir = System.getProperty(TMP_DIR);
            logger.warning(String.format("defaulting tlb tmp directory to %s", tmpDir));
        }
        logger.info(String.format("using %s as tlb temp directory", tmpDir));
        createDirIfNecessary(tmpDir);
        return tmpDir;
    }

    private void createDirIfNecessary(String tmpDir) {
        logger.info(String.format("checking for existance of directory %s as tlb tmpdir", tmpDir));
        File tmpDirectory = new File(tmpDir);
        if (tmpDirectory.exists()) {
            if (! tmpDirectory.isDirectory()) {
                String fileInsteedOfDirectoryMessage = String.format("tlb tmp dir %s is a file, it must be a directory", tmpDir);
                logger.warning(fileInsteedOfDirectoryMessage);
                throw new IllegalStateException(fileInsteedOfDirectoryMessage);
            }
            logger.info(String.format("directory %s exists, creation not required", tmpDir));
        } else {
            logger.info(String.format("directory %s doesn't exist, creating it now", tmpDir));
            try {
                FileUtils.forceMkdir(tmpDirectory);
            } catch (IOException e) {
                logger.log(Level.WARNING, String.format("could not create directory %s", tmpDirectory.getAbsolutePath()), e);
                throw new RuntimeException(e);
            }
            logger.info(String.format("created directory %s, which is to be used as tlb tmp dir.", tmpDir));
        }
    }

    public String classFileRelativePath(String testClass) {
        return testClass.replaceAll("\\.", "/") + ".class";
    }

    public File getUniqueFile(String seedString) {
        String fileName = DigestUtils.md5Hex(seedString);
        File file = new File(new File(tmpDir()), fileName);
        logger.info(String.format("unique file name %s translated to %s", seedString, file.getAbsolutePath()));
        return file;
    }
}
