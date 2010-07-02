package com.github.tlb.server;

import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SuiteEntryRepoTest {
    private static class TestCaseEntry implements SuiteLevelEntry {
        private final String testName;
        private final String suiteName;

        private TestCaseEntry(String testName, String suiteName) {
            this.testName = testName;
            this.suiteName = suiteName;
        }

        public String getName() {
            return testName;
        }

        public String dump() {
            return testName + "#" + suiteName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestCaseEntry that = (TestCaseEntry) o;

            if (suiteName != null ? !suiteName.equals(that.suiteName) : that.suiteName != null) return false;
            if (testName != null ? !testName.equals(that.testName) : that.testName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = testName != null ? testName.hashCode() : 0;
            result = 31 * result + (suiteName != null ? suiteName.hashCode() : 0);
            return result;
        }
    }

    private static class TestCaseRepo extends SuiteEntryRepo<TestCaseEntry> {
        @Override
        public TestCaseEntry getEntry(String record) {
            String[] nameAndHost = record.split("#");
            return new TestCaseEntry(nameAndHost[0], nameAndHost[1]);
        }
    }
    
    private TestCaseRepo testCaseRepo;

    @Before
    public void setUp() {
        testCaseRepo = new TestCaseRepo();
    }

    @Test
    public void shouldRecordSuiteTimeWhenAdded() {
        testCaseRepo.add("shouldBar#Bar");
        testCaseRepo.add("shouldFoo#Foo");
        List<SuiteLevelEntry> entryList = sortedList();
        assertThat(entryList.size(), is(2));
        assertThat((TestCaseEntry) entryList.get(0), is(new TestCaseEntry("shouldBar", "Bar")));
        assertThat((TestCaseEntry) entryList.get(1), is(new TestCaseEntry("shouldFoo", "Foo")));
    }

    @Test
    public void shouldOverwriteExistingEntryIfAddedAgain() {
        testCaseRepo.add("shouldBar#Bar");
        testCaseRepo.add("shouldFoo#Foo");
        testCaseRepo.add("shouldBar#Foo");
        List<SuiteLevelEntry> entryList = sortedList();
        assertThat(entryList.size(), is(2));
        assertThat((TestCaseEntry) entryList.get(0), is(new TestCaseEntry("shouldBar", "Foo")));
        assertThat((TestCaseEntry) entryList.get(1), is(new TestCaseEntry("shouldFoo", "Foo")));
    }

    @Test
    public void shouldDumpDataOnGivenOutputStream() throws IOException, ClassNotFoundException {
        testCaseRepo.add("shouldBar#Bar");
        testCaseRepo.add("shouldFoo#Foo");
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        testCaseRepo.diskDump(new ObjectOutputStream(outStream));
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        ConcurrentHashMap<String, SuiteLevelEntry> subsetTimeEntries = (ConcurrentHashMap<String, SuiteLevelEntry>)inputStream.readObject();
        ConcurrentHashMap<String, SuiteLevelEntry> expected = new ConcurrentHashMap<String, SuiteLevelEntry>();
        expected.put("shouldBar", new TestCaseEntry("shouldBar", "Bar"));
        expected.put("shouldFoo", new TestCaseEntry("shouldFoo", "Foo"));
        assertThat(subsetTimeEntries, is(expected));
    }

    @Test
    public void shouldLoadFromInputStreamGiven() throws IOException, ClassNotFoundException {
        ConcurrentHashMap<String, TestCaseEntry> toBeLoaded = new ConcurrentHashMap<String, TestCaseEntry>();
        toBeLoaded.put("shouldBar", new TestCaseEntry("shouldBar", "Bar"));
        toBeLoaded.put("shouldFoo", new TestCaseEntry("shouldFoo", "Foo"));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(toBeLoaded);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        testCaseRepo.load(inStream);
        assertThat(sortedList(), is(listOf(new TestCaseEntry("shouldBar", "Bar"), new TestCaseEntry("shouldFoo", "Foo"))));
    }
    
    private List<SuiteLevelEntry> listOf(SuiteLevelEntry ... entries) {
        ArrayList<SuiteLevelEntry> list = new ArrayList<SuiteLevelEntry>();
        for (SuiteLevelEntry entry : entries) {
            list.add(entry);
        }
        return list;
    }

    private List<SuiteLevelEntry> sortedList() {
        ArrayList<SuiteLevelEntry> entryList = new ArrayList<SuiteLevelEntry>(testCaseRepo.list());
        Collections.sort(entryList, new Comparator<SuiteLevelEntry>() {
            public int compare(SuiteLevelEntry o1, SuiteLevelEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return entryList;
    }

}
