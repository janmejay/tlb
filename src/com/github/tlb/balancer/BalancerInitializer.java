package com.github.tlb.balancer;

import com.github.tlb.server.ServerInitializer;
import com.github.tlb.utils.SystemEnvironment;
import org.restlet.Component;

/**
 * @understands initializing Balancer tlb restlet app
 */
public class BalancerInitializer implements ServerInitializer {
    public BalancerInitializer(SystemEnvironment environment) {
        
    }

    public Component init() {
        return null;
    }
}
