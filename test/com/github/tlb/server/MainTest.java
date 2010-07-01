package com.github.tlb.server;

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

import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

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

    class TestMain extends Main {

        TestMain(SystemEnvironment env) {
            super(env);
        }

        @Override
        Context appContext() {
            return context;
        }
    }

    @Test
    public void shouldStartContextReturnedByInit() {
        TestMain main = new TestMain(new SystemEnvironment());
        RouteList routeList = main.init().getDefaultHost().getRoutes();
        assertThat(routeList.size(), is(1));
        Restlet application = routeList.get(0).getNext();
        assertThat(application, is(TlbApplication.class));
        assertThat(application.getContext(), sameInstance(context));
    }
}
