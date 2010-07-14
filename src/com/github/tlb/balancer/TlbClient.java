package com.github.tlb.balancer;

import com.github.tlb.balancer.repo.BalancerResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

/**
 * @understands restlet tlb application for client that runs locally and offloads actual balancing logic from the client 
 */
public class TlbClient extends Application {

    public TlbClient(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        router.attach("/balance", BalancerResource.class);

        return router;
    }

    public static interface Balancer {
        static final String SPLITTER = "SPLITTER";
        static final String ORDERER = "ORDERER";
    }
}