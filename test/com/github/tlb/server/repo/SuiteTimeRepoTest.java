package com.github.tlb.server.repo;

import com.github.tlb.TestUtil;
import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.github.tlb.TestUtil.sortedList;
import static com.github.tlb.server.repo.EntryRepoFactory.LATEST_VERSION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class SuiteTimeRepoTest {
    private SuiteTimeRepo repo;
    protected File tmpDir;
    private EntryRepoFactory factory;

    @Before
    public void setUp() throws Exception {
        tmpDir = TestUtil.createTempFolder();
        factory = new EntryRepoFactory(tmpDir);
        repo = factory.createSuiteTimeRepo("name", LATEST_VERSION);
    }

    @Test
    public void shouldReturnSuiteTimeEntryForGivenRecord() {
        assertThat(repo.getEntry("foo.bar.Baz: 102"), is(new SuiteTimeEntry("foo.bar.Baz", 102l)));
        assertThat(repo.getEntry("bar.baz.Quux: 15"), is(new SuiteTimeEntry("bar.baz.Quux", 15l)));
    }

    @Test
    public void shouldKeepVersionFrozenAcrossDumpAndReload() throws InterruptedException, ClassNotFoundException, IOException {
        repo.update("foo.bar.Foo: 12");
        repo.update("foo.bar.Bar: 134");
        sortedList(repo.list("foo"));
        repo.update("foo.bar.Baz: 15");
        repo.update("foo.bar.Bar: 18");
        final Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        final SuiteTimeRepo newTestCaseRepo = new EntryRepoFactory(tmpDir).createSuiteTimeRepo("name", LATEST_VERSION);
        final List<SuiteLevelEntry> frozenCollection = sortedList(newTestCaseRepo.list("foo"));
        assertThat(frozenCollection.size(), is(2));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new SuiteTimeEntry("foo.bar.Foo", 12)));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new SuiteTimeEntry("foo.bar.Bar", 134)));
    }
}
