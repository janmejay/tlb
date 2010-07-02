package com.github.tlb.server;

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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
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
    public void shouldReturnSubsetSizeRepo() throws ClassNotFoundException, IOException {
        assertThat(factory.createSubsetRepo("dev"), is(not(nullValue())));
    }
    
    @Test
    public void shouldReturnSuiteTimeRepo() throws ClassNotFoundException, IOException {
        assertThat(factory.createSuiteTimeRepo("dev"), is(not(nullValue())));
    }
    
    @Test
    public void shouldNotOverrideSubsetRepoWithSuiteTimeRepo() throws ClassNotFoundException, IOException {
        SubsetSizeRepo subsetRepo = factory.createSubsetRepo("dev");
        SuiteTimeRepo suiteTimeRepo = factory.createSuiteTimeRepo("dev");
        assertThat(factory.createSubsetRepo("dev"), sameInstance(subsetRepo));
        assertThat(factory.createSuiteTimeRepo("dev"), sameInstance(suiteTimeRepo));
    }

    @Test
    public void shouldReturnOneRepositoryForOneFamilyName() throws ClassNotFoundException, IOException {
        assertThat(factory.createSubsetRepo("dev"), sameInstance(factory.createSubsetRepo("dev")));
    }

    @Test
    public void shouldCallDiskDumpForEachRepoAtExit() throws InterruptedException, IOException {
        SubsetSizeRepo repoFoo = mock(SubsetSizeRepo.class);
        SuiteTimeRepo repoBar = mock(SuiteTimeRepo.class);
        factory.getRepos().put("foo", repoFoo);
        factory.getRepos().put("bar", repoBar);
        Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        verify(repoFoo).diskDump(any(ObjectOutputStream.class));
        verify(repoBar).diskDump(any(ObjectOutputStream.class));
    }
    
    @Test
    public void shouldBeAbleToLoadFromDumpedFile() throws ClassNotFoundException, IOException, InterruptedException {
        SubsetSizeRepo repo = factory.createSubsetRepo("foo");
        repo.add("50");
        repo.add("100");
        repo.add("200");
        Thread exitHook = factory.exitHook();
        exitHook.start();
        exitHook.join();
        EntryRepoFactory otherFactoryInstance = new EntryRepoFactory(baseDir);
        SubsetSizeRepo otherRepo = otherFactoryInstance.createSubsetRepo("foo");
        assertThat(otherRepo.list(), is((Collection<Integer>) Arrays.asList(50, 100, 200)));
    }
    
    @Test
    public void shouldLogExceptionsButContinueDumpingRepositories() throws InterruptedException, IOException {
        SubsetSizeRepo repoFoo = mock(SubsetSizeRepo.class);
        SubsetSizeRepo repoBar = mock(SubsetSizeRepo.class);
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
        File file = new File(baseDir, EntryRepoFactory.name("foo", EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        outStream.close();
        SubsetSizeRepo repo = factory.createSubsetRepo("foo");
        assertThat(repo.list(), is((Collection<Integer>) Arrays.asList(1, 2, 3)));
    }
    
    @Test
    public void shouldNotLoadDiskDumpWhenUsingARepoThatIsAlreadyCreated() throws ClassNotFoundException, IOException {
        SubsetSizeRepo fooRepo = factory.createSubsetRepo("foo");
        File file = new File(baseDir, EntryRepoFactory.name("foo", EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        outStream.close();
        assertThat(fooRepo.list().size(), is(0));
        assertThat(factory.createSubsetRepo("foo").list().size(), is(0));
    }
}
