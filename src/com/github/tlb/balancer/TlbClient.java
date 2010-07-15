package com.github.tlb.balancer;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

/**
 * @understands restlet tlb application for client that runs locally and offloads actual balancing logic from the client 
 */
public class TlbClient extends Application {
    public static final String SPLITTER = "SPLITTER";
    public static final String ORDERER = "ORDERER";
    public static final String TALK_TO_SERVICE = "TALK_TO_SERVICE";

    public TlbClient(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        router.attach("/balance", BalancerResource.class);
        router.attach("/suite_time", SuiteTimeReporter.class);

        return router;
    }
}