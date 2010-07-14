package com.github.tlb;

import java.io.File;

/**
 * @understands a file resource which needs to be filtered
 */
public interface TlbFileResource {
    String getName();

    File getFile();

    void setBaseDir(File file);
}
