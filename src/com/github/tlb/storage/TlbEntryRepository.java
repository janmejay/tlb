package com.github.tlb.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @understands storing and retrieving entries
 */
public class TlbEntryRepository {
    private static final Logger logger = Logger.getLogger(TlbEntryRepository.class.getName());
    private File directory;
    private String fileName;

    public TlbEntryRepository(String directory, String fileName) {
        this.directory = new File(directory);
        this.fileName = fileName;
    }

    public void appendLine(String line) {
        File cacheFile = getFile();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(cacheFile, true);
            IOUtils.write(line, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        logger.info(String.format("Wrote [ %s ] to %s", line, cacheFile.getAbsolutePath()));
    }

    public List<String> load() {
        File cacheFile = getFile();
        FileInputStream in = null;
        List<String> lines = null;
        if (!cacheFile.exists()) {
            return new ArrayList<String>();
        }
        try {
            in = new FileInputStream(cacheFile);
            lines = IOUtils.readLines(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        logger.info(String.format("Cached %s lines from %s, the last of which was [ %s ]", lines.size(), cacheFile.getAbsolutePath(), lastLine(lines)));
        return lines;
    }

    public File getFile() {
        return new File(directory, fileName);
    }

    public void cleanup() throws IOException {
        FileUtils.forceDelete(getFile());
    }

    public String loadLastLine() {
        return lastLine(load());
    }

    private String lastLine(List<String> lines) {
        return lines.get(lines.size() - 1);
    }
}
