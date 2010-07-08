package com.github.tlb.server.repo;

import com.github.tlb.TestUtil;
import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.domain.TimeProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;

import static com.github.tlb.server.repo.EntryRepoFactory.LATEST_VERSION;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class EntryRepoFactoryTest {
    private EntryRepoFactory factory;
    private File baseDir;
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() throws Exception {
        baseDir = TestUtil.createTempFolder();
        factory = new EntryRepoFactory(baseDir);
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldPassFactoryAndNamespaceToEachRepo() throws ClassNotFoundException, IOException {
        final EntryRepo createdEntryRepo = mock(EntryRepo.class);
        final EntryRepo repo = factory.findOrCreate("namespace", "old_version", "suite_time", new EntryRepoFactory.Creator<EntryRepo>() {
            public EntryRepo create() {
                return createdEntryRepo;
            }
        });
        assertThat(repo, sameInstance(createdEntryRepo));
        verify(createdEntryRepo).setFactory(factory);
        verify(createdEntryRepo).setNamespace("namespace");
        verify(createdEntryRepo).setIdentifier("namespace|old_version|suite_time");
    }

    @Test
    public void shouldNotOverrideSubsetRepoWithSuiteTimeRepo() throws ClassNotFoundException, IOException {
        SubsetSizeRepo subsetRepo = factory.createSubsetRepo("dev", LATEST_VERSION);
        SuiteTimeRepo suiteTimeRepo = factory.createSuiteTimeRepo("dev", LATEST_VERSION);
        SuiteResultRepo suiteResultRepo = factory.createSuiteResultRepo("dev", LATEST_VERSION);
        assertThat(factory.createSubsetRepo("dev", LATEST_VERSION), sameInstance(subsetRepo));
        assertThat(factory.createSuiteTimeRepo("dev", LATEST_VERSION), sameInstance(suiteTimeRepo));
        assertThat(factory.createSuiteResultRepo("dev", LATEST_VERSION), sameInstance(suiteResultRepo));
    }

    @Test
    public void shouldReturnOneRepositoryForOneFamilyName() throws ClassNotFoundException, IOException {
        assertThat(factory.createSubsetRepo("dev", LATEST_VERSION), sameInstance(factory.createSubsetRepo("dev", LATEST_VERSION)));
    }

    @Test
    public void shouldCallDiskDumpForEachRepoAtExit() throws InterruptedException, IOException {
        EntryRepo repoFoo = mock(EntryRepo.class);
        EntryRepo repoBar = mock(EntryRepo.class);
        EntryRepo repoBaz = mock(EntryRepo.class);
        factory.getRepos().put("foo", repoFoo);
        factory.getRepos().put("bar", repoBar);
        factory.getRepos().put("baz", repoBaz);
        Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        verify(repoFoo).diskDump(any(ObjectOutputStream.class));
        verify(repoBar).diskDump(any(ObjectOutputStream.class));
        verify(repoBaz).diskDump(any(ObjectOutputStream.class));
    }
    
    @Test
    public void shouldBeAbleToLoadFromDumpedFile() throws ClassNotFoundException, IOException, InterruptedException {
        SubsetSizeRepo repo = factory.createSubsetRepo("foo", LATEST_VERSION);
        repo.add(new SubsetSizeEntry(50));
        repo.add(new SubsetSizeEntry(100));
        repo.add(new SubsetSizeEntry(200));
        Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        EntryRepoFactory otherFactoryInstance = new EntryRepoFactory(baseDir);
        SubsetSizeRepo otherRepo = otherFactoryInstance.createSubsetRepo("foo", LATEST_VERSION);
        assertThat(otherRepo.list(), is((Collection<SubsetSizeEntry>) Arrays.asList(new SubsetSizeEntry(50), new SubsetSizeEntry(100), new SubsetSizeEntry(200))));
    }
    
    @Test
    public void shouldLogExceptionsButContinueDumpingRepositories() throws InterruptedException, IOException {
        EntryRepo repoFoo = mock(EntryRepo.class);
        EntryRepo repoBar = mock(EntryRepo.class);
        factory.getRepos().put("foo|subset_size", repoFoo);
        factory.getRepos().put("bar|subset_size", repoBar);
        doThrow(new IOException("test exception")).when(repoFoo).diskDump(any(ObjectOutputStream.class));
        logFixture.startListening();
        factory.run();
        logFixture.stopListening();
        logFixture.assertHeard("disk dump of foo|subset_size failed");
        verify(repoFoo).diskDump(any(ObjectOutputStream.class));
        verify(repoBar).diskDump(any(ObjectOutputStream.class));
    }

    @Test
    public void shouldWireUpAtExitHook() {
        factory.registerExitHook();
        try {
            Runtime.getRuntime().addShutdownHook(factory.exitHook());
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Hook previously registered"));
        }
    }
    
    @Test
    public void shouldFeedTheDiskDumpContentsToSubsetRepo() {
        TestUtil.createTempFolder();
    }

    @Test
    public void shouldUsePresentWorkingDirectoryAsDiskStorageRoot() throws IOException, ClassNotFoundException {
        File file = new File(baseDir, EntryRepoFactory.name("foo", LATEST_VERSION, EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<SubsetSizeEntry>(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
        outStream.close();
        SubsetSizeRepo repo = factory.createSubsetRepo("foo", LATEST_VERSION);
        assertThat(repo.list(), is((Collection<SubsetSizeEntry>) Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
    }
    
    @Test
    public void shouldNotLoadDiskDumpWhenUsingARepoThatIsAlreadyCreated() throws ClassNotFoundException, IOException {
        SubsetSizeRepo fooRepo = factory.createSubsetRepo("foo", LATEST_VERSION);
        File file = new File(baseDir, EntryRepoFactory.name("foo", LATEST_VERSION, EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<SubsetSizeEntry>(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
        outStream.close();
        assertThat(fooRepo.list().size(), is(0));
        assertThat(factory.createSubsetRepo("foo", LATEST_VERSION).list().size(), is(0));
    }

    @Test
    public void shouldPurgeDiskDumpAndRepositoryWhenAsked() throws IOException, ClassNotFoundException, InterruptedException {
        SuiteTimeRepo fooRepo = factory.createSuiteTimeRepo("foo", LATEST_VERSION);
        fooRepo.update(new SuiteTimeEntry("foo.bar.Baz", 15));
        fooRepo.update(new SuiteTimeEntry("foo.bar.Quux", 80));
        final Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        factory.purge(fooRepo.identifier);
        fooRepo = factory.createSuiteTimeRepo("foo", LATEST_VERSION);
        assertThat(fooRepo.list().size(), is(0));
        fooRepo = new EntryRepoFactory(baseDir).createSuiteTimeRepo("foo", LATEST_VERSION);
        assertThat(fooRepo.list().size(), is(0));
    }

    @Test
    public void shouldPurgeDiskDumpAndRepositoryOlderThanGivenTime() throws IOException, ClassNotFoundException, InterruptedException {
        final TimeProvider timeProvider = mock(TimeProvider.class);
        final EntryRepoFactory factory = new EntryRepoFactory(baseDir, timeProvider);

        SuiteTimeRepo repo = factory.createSuiteTimeRepo("foo", LATEST_VERSION);
        repo.update(new SuiteTimeEntry("foo.bar.Baz", 15));
        repo.update(new SuiteTimeEntry("foo.bar.Quux", 80));

        when(timeProvider.now()).thenReturn(new GregorianCalendar(2010, 6, 7, 0, 37, 12));
        Collection<SuiteTimeEntry> oldList = repo.list("old");
        assertThat(oldList.size(), is(2));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Baz", 15)));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Quux", 80)));

        repo.update(new SuiteTimeEntry("foo.bar.Bang", 130));
        repo.update(new SuiteTimeEntry("foo.bar.Baz", 20));

        oldList = repo.list("old");
        assertThat(oldList.size(), is(2));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Baz", 15)));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Quux", 80)));


        when(timeProvider.now()).thenReturn(new GregorianCalendar(2010, 6, 9, 0, 37, 12));
        Collection<SuiteTimeEntry> notSoOld = repo.list("not_so_old");
        assertThat(notSoOld.size(), is(3));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Baz", 20)));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Quux", 80)));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Bang", 130)));

        repo.update(new SuiteTimeEntry("foo.bar.Foo", 12));

        final Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();

        when(timeProvider.now()).thenReturn(new GregorianCalendar(2010, 6, 10, 0, 37, 12));
        factory.purgeVersionsOlderThan(2);

        oldList = repo.list("old");
        assertThat(oldList.size(), is(4));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Baz", 20)));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Quux", 80)));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Bang", 130)));
        assertThat(oldList, hasItem(new SuiteTimeEntry("foo.bar.Foo", 12)));

        notSoOld = repo.list("not_so_old");
        assertThat(notSoOld.size(), is(3));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Baz", 20)));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Quux", 80)));
        assertThat(notSoOld, hasItem(new SuiteTimeEntry("foo.bar.Bang", 130)));
    }

    @Test
    public void shouldHaveATimerThatPurgesOldVersions() throws ClassNotFoundException, IOException {
        final VersioningEntryRepo repo1 = mock(VersioningEntryRepo.class);
        final VersioningEntryRepo repo2 = mock(VersioningEntryRepo.class);
        final VersioningEntryRepo repo3 = mock(VersioningEntryRepo.class);
        doThrow(new IOException("test exception")).when(repo2).purgeOldVersions(12);
        findOrCreateRepo(repo1, "foo");
        findOrCreateRepo(repo2, "bar");
        findOrCreateRepo(repo3, "baz");
        logFixture.startListening();
        factory.purgeVersionsOlderThan(12);
        logFixture.stopListening();
        verify(repo1).purgeOldVersions(12);
        verify(repo2).purgeOldVersions(12);
        verify(repo3).purgeOldVersions(12);
        logFixture.assertHeard("failed to delete older versions for repo identified by 'bar|LATEST|foo_bar'");
        logFixture.assertHeardException(new IOException("test exception"));
    }

    private EntryRepo findOrCreateRepo(final VersioningEntryRepo repo, String name) throws IOException, ClassNotFoundException {
        return factory.findOrCreate(name, LATEST_VERSION, "foo_bar", new EntryRepoFactory.Creator<EntryRepo>() {
            public EntryRepo create() {
                return repo;
            }
        });
    }
}
