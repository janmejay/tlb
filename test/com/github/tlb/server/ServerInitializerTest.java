package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.utils.SystemEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.util.RouteList;
import org.restlet.util.ServerList;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class ServerInitializerTest {
    protected Component component;
    protected TlbApplication app;

    @Before
    public void setUp() {
        app = new TlbApplication(new Context());
        class TestMain extends TlbServerInitializer {
            TestMain(SystemEnvironment env) {
                super(env);
            }

            @Override
            protected TlbApplication application() {
                return app;
            }

            @Override
            protected int appPort() {
                return 614;
            }
        }
        TestMain main = new TestMain(new SystemEnvironment());
        component = main.init();
    }

    @Test
    public void shouldInitializeTlbToRunOnConfiguredPort() {
        ServerList servers = component.getServers();
        assertThat(servers.size(), is(1));
        assertThat(servers.get(0).getPort(), is(614));
        assertThat(servers.get(0).getProtocols().size(), is(1));
        assertThat(servers.get(0).getProtocols().get(0), is(Protocol.HTTP));
    }

    @Test
    public void shouldStartApplicationReturnedByInit() {
        RouteList routeList = component.getDefaultHost().getRoutes();
        assertThat(routeList.size(), is(1));
        Restlet application = routeList.get(0).getNext();
        assertThat(application, sameInstance((Restlet) app));
    }

}
