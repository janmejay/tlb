package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.server.resources.*;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import static com.github.tlb.TlbConstants.Server.LISTING_VERSION;
import static com.github.tlb.TlbConstants.Server.REQUEST_NAMESPACE;

/**
 * @understands restlet tlb application
 */
public class TlbApplication extends Application {

    public TlbApplication(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());
        router.attach("/{" + REQUEST_NAMESPACE + "}/subset_size", SubsetSizeResource.class);
        router.attach("/{" + REQUEST_NAMESPACE + "}/suite_time", SuiteTimeResource.class);
        router.attach("/{" + REQUEST_NAMESPACE + "}/smoothed_suite_time", SmoothingSuiteTimeResource.class);
        router.attach("/{" + REQUEST_NAMESPACE + "}/suite_time/{" + LISTING_VERSION +  "}", VersionedSuiteTimeResource.class);
        router.attach("/{" + REQUEST_NAMESPACE + "}/suite_result", SuiteResultResource.class);
        return router;
    }
}
