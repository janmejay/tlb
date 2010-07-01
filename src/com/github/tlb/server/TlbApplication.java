package com.github.tlb.server;

import com.github.tlb.server.resources.ProcessSubsetSize;
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
        router.attach("/{family}/subset_size", ProcessSubsetSize.class);
        return router;
    }
}
