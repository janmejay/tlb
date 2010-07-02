package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.server.resources.SubsetSizeResource;
import com.github.tlb.server.resources.SuiteResultResource;
import com.github.tlb.server.resources.SuiteTimeResource;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

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
        router.attach("/{" + TlbConstants.Server.REQUEST_NAMESPACE + "}/subset_size", SubsetSizeResource.class);
        router.attach("/{" + TlbConstants.Server.REQUEST_NAMESPACE + "}/suite_time", SuiteTimeResource.class);
        router.attach("/{" + TlbConstants.Server.REQUEST_NAMESPACE + "}/suite_result", SuiteResultResource.class);
        return router;
    }
}
