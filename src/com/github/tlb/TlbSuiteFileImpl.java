package com.github.tlb;

import java.util.ArrayList;
import java.util.List;

/**
 * @understands a file resource which needs to be filtered
 */
public class TlbSuiteFileImpl implements TlbSuiteFile {
    private final String fileName;

    public TlbSuiteFileImpl(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return fileName;
    }

    public static List<TlbSuiteFile> parse(String list) {
        final ArrayList<TlbSuiteFile> files = new ArrayList<TlbSuiteFile>();
        for (String suiteFileName : list.split("\n")) {
            if (! suiteFileName.trim().isEmpty()) files.add(new TlbSuiteFileImpl(suiteFileName));
        }
        return files;
    }

    @Override
    public String toString() {
        return "TlbSuiteFileImpl{" +
                "fileName='" + fileName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TlbSuiteFileImpl that = (TlbSuiteFileImpl) o;

        if (!fileName.equals(that.fileName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    public String dump() {
        return fileName + "\n";
    }
}