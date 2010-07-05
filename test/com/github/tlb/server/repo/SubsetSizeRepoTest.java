package com.github.tlb.server.repo;

import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.server.repo.SubsetSizeRepo;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SubsetSizeRepoTest {
    private SubsetSizeRepo subsetSizeRepo;

    @Before
    public void setUp() throws Exception {
        subsetSizeRepo = new SubsetSizeRepo();
    }
    
    @Test
    public void shouldNotAllowUpdate() {
        try {
            subsetSizeRepo.update(new SubsetSizeEntry(10));
            fail("update should not have been allowed");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("update not allowed on repository"));
        }
    }

    @Test
    public void shouldListAddedEntries() {
        addToRepo();

        List<SubsetSizeEntry> entries = (List<SubsetSizeEntry>) subsetSizeRepo.list();

        assertListContents(entries);
    }

    private void addToRepo() {
        subsetSizeRepo.add(new SubsetSizeEntry(10));
        subsetSizeRepo.add(new SubsetSizeEntry(12));
        subsetSizeRepo.add(new SubsetSizeEntry(7));
    }

    private void assertListContents(List<SubsetSizeEntry> entries) {
        assertThat(entries.size(), is(3));
        assertThat(entries.get(0), is(new SubsetSizeEntry(10)));
        assertThat(entries.get(1), is(new SubsetSizeEntry(12)));
        assertThat(entries.get(2), is(new SubsetSizeEntry(7)));
    }

    @Test
    public void shouldDumpDataOnGivenOutputStream() throws IOException, ClassNotFoundException {
        addToRepo();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        subsetSizeRepo.diskDump(new ObjectOutputStream(outStream));
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        List<SubsetSizeEntry> subsetSizes = (List<SubsetSizeEntry>) inputStream.readObject();
        assertListContents(subsetSizes);
    }

    @Test
    public void shouldLoadFromInputStreamGiven() throws IOException, ClassNotFoundException {
        addToRepo();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(new ArrayList<SubsetSizeEntry>(Arrays.asList(new SubsetSizeEntry(10), new SubsetSizeEntry(12), new SubsetSizeEntry(7))));
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        subsetSizeRepo.load(inStream);
        assertListContents((List<SubsetSizeEntry>) subsetSizeRepo.list());
    }
    
    @Test
    public void shouldNotAllowVersioning() {
        try {
            subsetSizeRepo.list("crap");
            fail("should not have allowed versioning");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("versioning not allowed"));
        }
    }
}
