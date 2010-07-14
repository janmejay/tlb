package com.github.tlb.balancer;

import com.github.tlb.balancer.repo.BalancerResource;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.Restlet;

import java.util.HashMap;

import static com.github.tlb.TestUtil.getRoutePatternsAndResources;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

public class TlbClientTest {
    private TlbClient app;

    @Before
    public void setUp() {
        Context context = mock(Context.class);
        app = new TlbClient(context);
    }

    @Test
    public void shouldHaveRouteForBalancing() {
        HashMap<String, Restlet> routeMaping = getRoutePatternsAndResources(app);
        assertThat(routeMaping.keySet(), hasItem("/balance"));
        Restlet restlet = routeMaping.get("/balance");
        assertThat(((Finder)restlet).getTargetClass().getName(), is(BalancerResource.class.getName()));
    }
}
