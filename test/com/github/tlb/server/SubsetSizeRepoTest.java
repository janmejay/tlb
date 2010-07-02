package com.github.tlb.server;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SubsetSizeRepoTest {
    private SubsetSizeRepo subsetSizeRepo;

    @Before
    public void setUp() throws Exception {
        subsetSizeRepo = new SubsetSizeRepo();
    }

    @Test
    public void shouldListAddedEntries() {
        addToRepo();

        List<Integer> entries = (List<Integer>) subsetSizeRepo.list();

        assertListContents(entries);
    }

    private void addToRepo() {
        subsetSizeRepo.add("10");
        subsetSizeRepo.add("12");
        subsetSizeRepo.add("7");
    }

    private void assertListContents(List<Integer> entries) {
        assertThat(entries.size(), is(3));
        assertThat(entries.get(0), is(10));
        assertThat(entries.get(1), is(12));
        assertThat(entries.get(2), is(7));
    }

    @Test
    public void shouldDumpDataOnGivenOutputStream() throws IOException, ClassNotFoundException {
        addToRepo();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        subsetSizeRepo.diskDump(new ObjectOutputStream(outStream));
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        ArrayList<Integer> subsetSizes = (ArrayList<Integer>) inputStream.readObject();
        assertListContents(subsetSizes);
    }

    @Test
    public void shouldLoadFromInputStreamGiven() throws IOException, ClassNotFoundException {
        addToRepo();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(new ArrayList<Integer>(Arrays.asList(10, 12, 7)));
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        subsetSizeRepo.load(inStream);
        assertListContents((List<Integer>) subsetSizeRepo.list());
    }
}
