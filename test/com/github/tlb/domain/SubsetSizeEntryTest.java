package com.github.tlb.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SubsetSizeEntryTest {
    @Test
    public void shouldDumpTheSubsetSize() {
        SubsetSizeEntry entry = new SubsetSizeEntry(10);
        assertThat(entry.dump(), is("10\n"));
    }

    @Test
    public void shouldParseSubsetSizes() {
        List<SubsetSizeEntry> sizeEntries = SubsetSizeEntry.parse("10\n20\n");
        assertThat(sizeEntries, is(Arrays.asList(new SubsetSizeEntry(10), new SubsetSizeEntry(20))));
    }
    
    @Test
    public void shouldParseSingleEntry() {
        SubsetSizeEntry entry = SubsetSizeEntry.parseSingleEntry("10");
        assertThat(entry.getSize(), is(10));
    }

    @Test
    public void shouldFailWhenCanNotParseSingleEntry() {
        try {
            SubsetSizeEntry.parseSingleEntry("abc");
            fail("should have failed as entry is unparsable");
        } catch (Exception e) {
            assertThat(e, is(NumberFormatException.class));
        }
    }

    @Test
    public void shouldNotFailForEmptyStringWhileParsingBuffer() {
        final List<SubsetSizeEntry> sizeEntries = SubsetSizeEntry.parse("10\n\n\n12\n\n");
        assertThat(sizeEntries.get(0), is(new SubsetSizeEntry(10)));
        assertThat(sizeEntries.get(1), is(new SubsetSizeEntry(12)));
        assertThat(sizeEntries.size(), is(2));
    }
}
