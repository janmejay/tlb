package com.github.tlb.server.repo;

import com.github.tlb.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.github.tlb.server.repo.EntryRepoFactory.LATEST_VERSION;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
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
        final EntryRepo repo = factory.findOrCreate("namespace", LATEST_VERSION, "suite_time", new EntryRepoFactory.Creator<EntryRepo>() {
            public EntryRepo create() {
                return createdEntryRepo;
            }
        });
        assertThat(repo, sameInstance(createdEntryRepo));
        verify(createdEntryRepo).setFactory(factory);
        verify(createdEntryRepo).setNamespace("namespace");
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
        repo.add("50");
        repo.add("100");
        repo.add("200");
        Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        EntryRepoFactory otherFactoryInstance = new EntryRepoFactory(baseDir);
        SubsetSizeRepo otherRepo = otherFactoryInstance.createSubsetRepo("foo", LATEST_VERSION);
        assertThat(otherRepo.list(), is((Collection<Integer>) Arrays.asList(50, 100, 200)));
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
        outStream.writeObject(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        outStream.close();
        SubsetSizeRepo repo = factory.createSubsetRepo("foo", LATEST_VERSION);
        assertThat(repo.list(), is((Collection<Integer>) Arrays.asList(1, 2, 3)));
    }
    
    @Test
    public void shouldNotLoadDiskDumpWhenUsingARepoThatIsAlreadyCreated() throws ClassNotFoundException, IOException {
        SubsetSizeRepo fooRepo = factory.createSubsetRepo("foo", LATEST_VERSION);
        File file = new File(baseDir, EntryRepoFactory.name("foo", LATEST_VERSION, EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        outStream.close();
        assertThat(fooRepo.list().size(), is(0));
        assertThat(factory.createSubsetRepo("foo", LATEST_VERSION).list().size(), is(0));
    }
}
