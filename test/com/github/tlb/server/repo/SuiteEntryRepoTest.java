package com.github.tlb.server.repo;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.TimeProvider;
import com.github.tlb.utils.SystemEnvironment;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tlb.server.repo.TestCaseRepo.TestCaseEntry.parseSingleEntry;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class SuiteEntryRepoTest {

    private TestCaseRepo testCaseRepo;

    @Before
    public void setUp() {
        testCaseRepo = new TestCaseRepo(new TimeProvider());
    }

    @Test
    public void shouldStoreAttributesFactorySets() throws ClassNotFoundException, IOException {
        final EntryRepoFactory factory = new EntryRepoFactory(new SystemEnvironment(Collections.singletonMap(TlbConstants.Server.TLB_STORE_DIR, TestUtil.createTempFolder().getAbsolutePath())));
        final SuiteEntryRepo entryRepo = (SuiteEntryRepo) factory.findOrCreate("name_space", "version", "type", new EntryRepoFactory.Creator<SuiteEntryRepo>() {
            public SuiteEntryRepo create() {
                return new SuiteEntryRepo<TestCaseRepo.TestCaseEntry>() {
                    public Collection<TestCaseRepo.TestCaseEntry> list(String version) throws IOException, ClassNotFoundException { return null; }
                };
            }
        });
        assertThat(entryRepo.factory, sameInstance(factory));
        assertThat(entryRepo.namespace, is("name_space"));
        assertThat(entryRepo.identifier, is("name_space|version|type"));
    }

    @Test
    public void shouldNotAllowAdditionOfEntries() {
        try {
            testCaseRepo.add(parseSingleEntry("shouldBar#Bar"));
            fail("add should not have been allowed for suite repo");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("add not allowed on repository"));
        }
    }

    @Test
    public void shouldRecordSuiteRecordWhenUpdated() {
        testCaseRepo.update(parseSingleEntry("shouldBar#Bar"));
        testCaseRepo.update(parseSingleEntry("shouldFoo#Foo"));
        List<SuiteLevelEntry> entryList = TestUtil.sortedList(testCaseRepo.list());
        assertThat(entryList.size(), is(2));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(0), is(new TestCaseRepo.TestCaseEntry("shouldBar", "Bar")));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(1), is(new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo")));
    }

    @Test
    public void shouldOverwriteExistingEntryIfAddedAgain() {
        testCaseRepo.update(parseSingleEntry("shouldBar#Bar"));
        testCaseRepo.update(parseSingleEntry("shouldFoo#Foo"));
        testCaseRepo.update(parseSingleEntry("shouldBar#Foo"));
        List<SuiteLevelEntry> entryList = TestUtil.sortedList(testCaseRepo.list());
        assertThat(entryList.size(), is(2));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(0), is(new TestCaseRepo.TestCaseEntry("shouldBar", "Foo")));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(1), is(new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo")));
    }

    @Test
    public void shouldDumpDataOnGivenOutputStream() throws IOException, ClassNotFoundException {
        testCaseRepo.update(parseSingleEntry("shouldBar#Bar"));
        testCaseRepo.update(parseSingleEntry("shouldFoo#Foo"));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        testCaseRepo.diskDump(new ObjectOutputStream(outStream));
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        ConcurrentHashMap<String, SuiteLevelEntry> subsetTimeEntries = (ConcurrentHashMap<String, SuiteLevelEntry>) inputStream.readObject();
        ConcurrentHashMap<String, SuiteLevelEntry> expected = new ConcurrentHashMap<String, SuiteLevelEntry>();
        expected.put("shouldBar", new TestCaseRepo.TestCaseEntry("shouldBar", "Bar"));
        expected.put("shouldFoo", new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo"));
        assertThat(subsetTimeEntries, is(expected));
    }

    @Test
    public void shouldLoadFromInputStreamGiven() throws IOException, ClassNotFoundException {
        ConcurrentHashMap<String, TestCaseRepo.TestCaseEntry> toBeLoaded = new ConcurrentHashMap<String, TestCaseRepo.TestCaseEntry>();
        toBeLoaded.put("shouldBar", new TestCaseRepo.TestCaseEntry("shouldBar", "Bar"));
        toBeLoaded.put("shouldFoo", new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo"));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new ObjectOutputStream(outStream).writeObject(toBeLoaded);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray()));
        testCaseRepo.load(inStream);
        assertThat(TestUtil.sortedList(testCaseRepo.list()), is(listOf(new TestCaseRepo.TestCaseEntry("shouldBar", "Bar"), new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo"))));
    }

    @Test
    public void shouldVersionListItself() {
        testCaseRepo.update(parseSingleEntry("shouldBar#Bar"));
        testCaseRepo.update(parseSingleEntry("shouldFoo#Foo"));
        List<SuiteLevelEntry> entryList = TestUtil.sortedList(testCaseRepo.list());
        assertThat(entryList.size(), is(2));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(0), is(new TestCaseRepo.TestCaseEntry("shouldBar", "Bar")));
        assertThat((TestCaseRepo.TestCaseEntry) entryList.get(1), is(new TestCaseRepo.TestCaseEntry("shouldFoo", "Foo")));

    }
    
    private List<SuiteLevelEntry> listOf(SuiteLevelEntry ... entries) {
        ArrayList<SuiteLevelEntry> list = new ArrayList<SuiteLevelEntry>();
        for (SuiteLevelEntry entry : entries) {
            list.add(entry);
        }
        return list;
    }
}
