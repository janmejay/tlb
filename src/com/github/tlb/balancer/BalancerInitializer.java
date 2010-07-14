package com.github.tlb.balancer;

import com.github.tlb.TlbConstants;
import com.github.tlb.factory.TlbFactory;
import com.github.tlb.server.ServerInitializer;
import com.github.tlb.utils.SystemEnvironment;
import org.restlet.Context;
import org.restlet.Restlet;

import java.util.HashMap;

/**
 * @understands initializing Balancer tlb restlet app
 */
public class BalancerInitializer extends ServerInitializer {
    private final SystemEnvironment env;

    public BalancerInitializer(SystemEnvironment env) {
        this.env = env;
    }

    @Override
    protected int appPort() {
        return Integer.parseInt(env.getProperty(TlbConstants.Balancer.TLB_BALANCER_PORT));
    }

    @Override
    public Restlet application() {
        HashMap<String, Object> appMap = new HashMap<String, Object>();
        appMap.put(TlbClient.Balancer.SPLITTER, TlbFactory.getCriteria(env.getProperty(TlbConstants.TLB_CRITERIA), env));
        appMap.put(TlbClient.Balancer.ORDERER, TlbFactory.getOrderer(env.getProperty(TlbConstants.TLB_ORDERER), env));
        Context applicationContext = new Context();
        applicationContext.setAttributes(appMap);
        return new TlbClient(applicationContext);
    }
}
