package com.github.tlb.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @understands number of tests run in a partition of a module
 */
public class SubsetSizeEntry implements Entry {
    private final int size;

    public SubsetSizeEntry(int size) {
        this.size = size;
    }

    public String dump() {
        return String.valueOf(size);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubsetSizeEntry that = (SubsetSizeEntry) o;

        if (size != that.size) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return size;
    }

    public static List<SubsetSizeEntry> parse(String text) {
        ArrayList<SubsetSizeEntry> entries = new ArrayList<SubsetSizeEntry>();
        for (String entry : text.split("\n")) {
            entries.add(new SubsetSizeEntry(Integer.parseInt(entry)));
        }
        return entries;
    }
}
