package com.github.tlb.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SubsetSizeEntryTest {
    @Test
    public void shouldDumpTheSubsetSize() {
        SubsetSizeEntry entry = new SubsetSizeEntry(10);
        assertThat(entry.dump(), is("10"));
    }

    @Test
    public void shouldParseSubsetSizes() {
        List<SubsetSizeEntry> sizeEntries = SubsetSizeEntry.parse("10\n20\n");
        assertThat(sizeEntries, is(Arrays.asList(new SubsetSizeEntry(10), new SubsetSizeEntry(20))));
    }
}
