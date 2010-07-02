package com.github.tlb.server;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import com.github.tlb.utils.SystemEnvironment;
import org.hamcrest.core.IsSame;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.util.RouteList;
import org.restlet.util.ServerList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainTest {
    private Main main;
    private HashMap<String, String> systemEnv;
    private Context context = new Context();

    @Before
    public void setUp() {
        systemEnv = new HashMap<String, String>();
        SystemEnvironment env = new SystemEnvironment(systemEnv);
        main = new Main(env);
    }

    @Test
    public void shouldCreateApplicationContextWithRepoFactory() {
        ConcurrentMap<String,Object> map = main.appContext().getAttributes();
        assertThat(map.get(TlbConstants.Server.REPO_FACTORY), is(EntryRepoFactory.class));
    }

    @Test
    public void shouldInitializeTlbToRunOnConfiguredPort() {
        systemEnv.put(TlbConstants.TLB_PORT, "1234");
        Component component = main.init();
        ServerList servers = component.getServers();
        assertThat(servers.size(), is(1));
        assertThat(servers.get(0).getPort(), is(1234));
        assertThat(servers.get(0).getProtocols().size(), is(1));
        assertThat(servers.get(0).getProtocols().get(0), is(Protocol.HTTP));
    }

    @Test
    public void shouldInitializeTlbWithDefaultPortIfNotGiven() {
        Component component = main.init();
        ServerList servers = component.getServers();
        assertThat(servers.size(), is(1));
        assertThat(servers.get(0).getPort(), is(7019));
    }


    @Test
    public void shouldStartContextReturnedByInit() {
        class TestMain extends Main {
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
        class TestMain extends Main {
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
        EntryRepoFactory factory = main.repoFactory();
        File dir = TestUtil.mkdirInPwd("tlb_store");
        File file = new File(dir, EntryRepoFactory.name("foo", EntryRepoFactory.SUBSET_SIZE));
        ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(file));
        outStream.writeObject(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        outStream.close();
        SubsetEntryRepo repo = factory.createSubsetRepo("foo");
        assertThat(repo.list(), is(Arrays.asList(1, 2, 3)));
    }
}
