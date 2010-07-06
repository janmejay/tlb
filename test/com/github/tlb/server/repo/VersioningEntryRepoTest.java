package com.github.tlb.server.repo;

import com.github.tlb.TestUtil;
import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.TimeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import static com.github.tlb.TestUtil.sortedList;
import static com.github.tlb.server.repo.EntryRepoFactory.LATEST_VERSION;
import static com.github.tlb.server.repo.TestCaseRepo.TestCaseEntry.parseSingleEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class VersioningEntryRepoTest {
    private TestCaseRepo repo;
    protected EntryRepoFactory factory;
    protected File tmpDir;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        tmpDir = TestUtil.createTempFolder();
        factory = new EntryRepoFactory(tmpDir);
        repo = createRepo(factory);
        repo.update(parseSingleEntry("shouldBar#Bar"));
        repo.update(parseSingleEntry("shouldBaz#Baz"));
    }

    @Test
    public void shouldKillVersionsOlderThanACertainAge() throws ClassNotFoundException, IOException {
        final TimeProvider timeProvider = mock(TimeProvider.class);
        repo = new TestCaseRepo(timeProvider);
        final EntryRepoFactory factory = mock(EntryRepoFactory.class);
        repo.setFactory(factory);
        repo.setNamespace("foo");
        GregorianCalendar cal = new GregorianCalendar(2010, 6, 6, 0, 35, 15);
        when(timeProvider.now()).thenReturn(cal);
        final TestCaseRepo versionedRepo = new TestCaseRepo(timeProvider);
        versionedRepo.setFactory(factory);
        versionedRepo.setNamespace("foo");
        versionedRepo.setIdentifier("foo|1.1|test_case");
        when(factory.findOrCreate(eq("foo"), eq("1.1"), eq("test_case"), any(EntryRepoFactory.Creator.class))).thenReturn(versionedRepo);
        repo.list("1.1");
        verify(factory, new Times(1)).findOrCreate(eq("foo"), eq("1.1"), eq("test_case"), any(EntryRepoFactory.Creator.class));
        //when not too old, doesn't get killed
        cal = new GregorianCalendar(2010, 6, 7, 0, 35, 14);
        when(timeProvider.now()).thenReturn(cal);
        repo.purgeOldVersions(1);
        verify(factory, never()).purge("foo|1.1|test_case");
        repo.list("1.1");
        verify(factory, new Times(1)).findOrCreate(eq("foo"), eq("1.1"), eq("test_case"), any(EntryRepoFactory.Creator.class));

        //when not too old, doesn't get killed
        cal = new GregorianCalendar(2010, 6, 8, 0, 35, 14);
        when(timeProvider.now()).thenReturn(cal);
        repo.purgeOldVersions(2);
        verify(factory, never()).purge("foo|1.1|test_case");
        repo.list("1.1");
        verify(factory, new Times(1)).findOrCreate(eq("foo"), eq("1.1"), eq("test_case"), any(EntryRepoFactory.Creator.class));

        //when too old, does get removed
        cal = new GregorianCalendar(2010, 6, 7, 0, 35, 16);
        when(timeProvider.now()).thenReturn(cal);
        repo.purgeOldVersions(1);
        verify(factory).purge("foo|1.1|test_case");
        repo.list("1.1");
        verify(factory, new Times(2)).findOrCreate(eq("foo"), eq("1.1"), eq("test_case"), any(EntryRepoFactory.Creator.class));
    }

    @Test
    public void shouldReturnListAsAtTheTimeOfQueryingAVersion() throws ClassNotFoundException, IOException {
        final List<SuiteLevelEntry> frozenCollection = sortedList(repo.list("foo"));
        assertThat(frozenCollection.size(), is(2));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBar", "Bar")));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBaz", "Baz")));
    }

    @Test
    public void shouldFreezeAVersionOnceCreated() throws ClassNotFoundException, IOException {
        sortedList(repo.list("foo"));
        repo.update(parseSingleEntry("shouldFoo#Foo"));
        repo.update(parseSingleEntry("shouldBaz#Quux"));
        final List<SuiteLevelEntry> frozenCollection = sortedList(repo.list("foo"));
        assertThat(frozenCollection.size(), is(2));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBar", "Bar")));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBaz", "Baz")));
    }

    @Test
    public void shouldKeepVersionFrozenAcrossDumpAndReload() throws InterruptedException, ClassNotFoundException, IOException {
        sortedList(repo.list("foo"));
        repo.update(parseSingleEntry("shouldFoo#Foo"));
        repo.update(parseSingleEntry("shouldBaz#Quux"));
        final Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        final TestCaseRepo newTestCaseRepo = createRepo(new EntryRepoFactory(tmpDir));
        final List<SuiteLevelEntry> frozenCollection = sortedList(newTestCaseRepo.list("foo"));
        assertThat(frozenCollection.size(), is(2));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBar", "Bar")));
        assertThat(frozenCollection, hasItem((SuiteLevelEntry) new TestCaseRepo.TestCaseEntry("shouldBaz", "Baz")));
    }

    @Test
    public void shouldSetFactoryAndNamespace() {
        assertThat(repo.getNamespace(), is("foo"));
        assertThat(repo.getFactory(), sameInstance(factory));
    }
    
    private TestCaseRepo createRepo(final EntryRepoFactory factory) throws IOException, ClassNotFoundException {
        return (TestCaseRepo) factory.findOrCreate("foo", LATEST_VERSION, "test_case", new EntryRepoFactory.Creator<TestCaseRepo>() {
            public TestCaseRepo create() {
                return new TestCaseRepo(new TimeProvider());
            }
        });
    }
}
