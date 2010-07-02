package com.github.tlb.server;

import com.github.tlb.domain.SuiteTimeEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SuiteTimeRepoTest {
    private SuiteTimeRepo suiteTimeRepo;

    @Before
    public void setUp() {
        suiteTimeRepo = new SuiteTimeRepo();
    }

    @Test
    public void shouldRecordSuiteTimeWhenAdded() {
        suiteTimeRepo.add("com.foo.Bar: 14");
        suiteTimeRepo.add("com.foo.Foo: 150");
        List<SuiteTimeEntry> entryList = sortedList();
        assertThat(entryList.size(), is(2));
        assertThat(entryList.get(0), is(new SuiteTimeEntry("com.foo.Bar", 14)));
        assertThat(entryList.get(1), is(new SuiteTimeEntry("com.foo.Foo", 150)));
    }

    @Test
    public void shouldOverwriteExistingEntryIfAddedAgain() {
        suiteTimeRepo.add("com.foo.Bar: 14");
        suiteTimeRepo.add("com.foo.Foo: 150");
        suiteTimeRepo.add("com.foo.Bar: 30");
        List<SuiteTimeEntry> entryList = sortedList();
        assertThat(entryList.size(), is(2));
        assertThat(entryList.get(0), is(new SuiteTimeEntry("com.foo.Bar", 30)));
        assertThat(entryList.get(1), is(new SuiteTimeEntry("com.foo.Foo", 150)));
    }

    @Test
    public void shouldDumpDataOnGivenOutputStream() throws IOException, ClassNotFoundException {
        suiteTimeRepo.add("com.foo.Bar: 14");
        suiteTimeRepo.add("com.foo.Foo: 150");
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        suiteTimeRepo.diskDump(new ObjectOutputStream(outStream));
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        ConcurrentHashMap<String, SuiteTimeEntry> subsetTimeEntries = (ConcurrentHashMap<String, SuiteTimeEntry>)inputStream.readObject();
        ConcurrentHashMap<String, SuiteTimeEntry> expected = new ConcurrentHashMap<String, SuiteTimeEntry>();
        expected.put("com.foo.Bar", new SuiteTimeEntry("com.foo.Bar", 14));
        expected.put("com.foo.Foo", new SuiteTimeEntry("com.foo.Foo", 150));
        assertThat(subsetTimeEntries, is(expected));
    }

    @Test
    public void shouldLoadFromInputStreamGiven() throws IOException, ClassNotFoundException {
        ConcurrentHashMap<String, SuiteTimeEntry> toBeLoaded = new ConcurrentHashMap<String, SuiteTimeEntry>();
        toBeLoaded.put("com.foo.Bar", new SuiteTimeEntry("com.foo.Bar", 14));
        toBeLoaded.put("com.foo.Foo", new SuiteTimeEntry("com.foo.Foo", 150));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(toBeLoaded);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        suiteTimeRepo.load(inStream);
        assertThat(sortedList(), is(Arrays.asList(new SuiteTimeEntry("com.foo.Bar", 14), new SuiteTimeEntry("com.foo.Foo", 150))));
    }

    private List<SuiteTimeEntry> sortedList() {
        List<SuiteTimeEntry> entryList = new ArrayList<SuiteTimeEntry>(suiteTimeRepo.list());
        Collections.sort(entryList, new Comparator<SuiteTimeEntry>() {
            public int compare(SuiteTimeEntry o1, SuiteTimeEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return entryList;
    }

}
