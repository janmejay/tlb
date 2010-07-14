package com.github.tlb.server;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SubsetSizeRepo;
import com.github.tlb.utils.SystemEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.util.RouteList;
import org.restlet.util.ServerList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static com.github.tlb.TlbConstants.Server.VERSION_LIFE_IN_DAYS;
import static com.github.tlb.server.repo.EntryRepoFactory.LATEST_VERSION;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TlbServerInitializerTest {
    private TlbServerInitializer initializer;
    private HashMap<String, String> systemEnv;
    private Context context = new Context();

    @Before
    public void setUp() {
        systemEnv = new HashMap<String, String>();
        SystemEnvironment env = new SystemEnvironment(systemEnv);
        initializer = new TlbServerInitializer(env);
    }

    @Test
    public void shouldCreateApplicationContextWithRepoFactory() {
        ConcurrentMap<String,Object> map = initializer.appContext().getAttributes();
        assertThat(map.get(TlbConstants.Server.REPO_FACTORY), is(EntryRepoFactory.class));
    }

    @Test
    public void shouldInitializeTlbToRunOnConfiguredPort() {
        systemEnv.put(TlbConstants.Server.TLB_PORT, "1234");
        Component component = initializer.init();
        ServerList servers = component.getServers();
        assertThat(servers.size(), is(1));
        assertThat(servers.get(0).getPort(), is(1234));
        assertThat(servers.get(0).getProtocols().size(), is(1));
        assertThat(servers.get(0).getProtocols().get(0), is(Protocol.HTTP));
    }

    @Test
    public void shouldInitializeTlbWithDefaultPortIfNotGiven() {
        Component component = initializer.init();
        ServerList servers = component.getServers();
        assertThat(servers.size(), is(1));
        assertThat(servers.get(0).getPort(), is(7019));
    }


    @Test
    public void shouldStartContextReturnedByInit() {
        class TestMain extends TlbServerInitializer {
            TestMain(SystemEnvironment env) {
                super(env);
            }

            @Override
            Context appContext() {
                return context;
            }
        }
        TestMain main = new TestMain(new SystemEnvironment());
        RouteList routeList = main.init().getDefaultHost().getRoutes();
        assertThat(routeList.size(), is(1));
        Restlet application = routeList.get(0).getNext();
        assertThat(application.getContext(), sameInstance(context));
    }

    @Test
    public void shouldRegisterEntryRepoFactoryExitHook() {
        final EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        class TestMain extends TlbServerInitializer {
            TestMain(SystemEnvironment env) {
                super(env);
            }

            @Override
            EntryRepoFactory repoFactory() {
                return repoFactory;
            }
        }
        TestMain main = new TestMain(new SystemEnvironment());
        Context ctx = main.appContext();
        assertThat((EntryRepoFactory) ctx.getAttributes().get(TlbConstants.Server.REPO_FACTORY), sameInstance(repoFactory));
        verify(repoFactory).registerExitHook();
    }

    @Test
    public void shouldInitializeEntryRepoFactoryWithPresentWorkingDirectoryAsDiskStorageRoot() throws IOException, ClassNotFoundException {
        EntryRepoFactory factory = initializer.repoFactory();
        File dir = TestUtil.mkdirInPwd("tlb_store");
        File file = new File(dir, EntryRepoFactory.name("foo", LATEST_VERSION, EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<SubsetSizeEntry>(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
        outStream.close();
        SubsetSizeRepo repo = factory.createSubsetRepo("foo", LATEST_VERSION);
        assertThat((List<SubsetSizeEntry>) repo.list(), is(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
    }

    @Test
    public void shouldHonorDiskStorageRootOverride() throws IOException, ClassNotFoundException {
        String tmpDir = TestUtil.createTempFolder().getAbsolutePath();
        systemEnv.put(TlbConstants.Server.TLB_STORE_DIR, tmpDir);
        EntryRepoFactory factory = initializer.repoFactory();
        File file = new File(tmpDir, EntryRepoFactory.name("quux", LATEST_VERSION, EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<SubsetSizeEntry>(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
        outStream.close();
        SubsetSizeRepo repo = factory.createSubsetRepo("quux", LATEST_VERSION);
        assertThat((List<SubsetSizeEntry>) repo.list(), is(Arrays.asList(new SubsetSizeEntry(1), new SubsetSizeEntry(2), new SubsetSizeEntry(3))));
    }
    
    @Test
    public void shouldEscapeTheEscapeCharInName() {
        assertThat(EntryRepoFactory.name("foo", "bar", "baz"), is("foo|bar|baz"));
        assertThat(EntryRepoFactory.name("fo|o", "b|ar", "baz|"), is("fo||o|b||ar|baz||"));
    }

    @Test
    public void shouldSetTimerToPurgeOldVersions() {
        final TimerTask[] tasks = new TimerTask[1];
        final EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        final Timer timer = new Timer() {
            @Override
            public void schedule(TimerTask task, long delay, long period) {
                tasks[0] = task;
                assertThat(delay, is(0l));
                assertThat(period, is(1*24*60*60*1000l));
            }
        };

        new TlbServerInitializer(new SystemEnvironment(systemEnv), timer) {
            @Override
            EntryRepoFactory repoFactory() {
                return repoFactory;
            }
        }.init();

        tasks[0].run();
        verify(repoFactory).purgeVersionsOlderThan(1);

        systemEnv.put(VERSION_LIFE_IN_DAYS, "3");

        new TlbServerInitializer(new SystemEnvironment(systemEnv), timer) {
            @Override
            EntryRepoFactory repoFactory() {
                return repoFactory;
            }
        }.init();

        tasks[0].run();
        verify(repoFactory).purgeVersionsOlderThan(3);
    }
}
