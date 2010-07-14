package com.github.tlb;

import java.io.File;

/**
 * @understands a file resource which needs to be filtered
 */
public interface TlbFileResource extends TlbSuiteFile {
    File getFile();

    void setBaseDir(File file);
}
