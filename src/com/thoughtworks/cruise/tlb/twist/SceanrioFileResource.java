package com.thoughtworks.cruise.tlb.twist;

import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.io.File;

/**
 * ADD_UNDERSTANDS_BLOCK
 */
public class SceanrioFileResource implements TlbFileResource {
    private File file;

    public SceanrioFileResource(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public void setBaseDir(File file) {
        //no op
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SceanrioFileResource that = (SceanrioFileResource) o;
        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
