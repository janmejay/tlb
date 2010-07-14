package com.github.tlb.server;

import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.resources.*;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

import static com.github.tlb.TlbConstants.Server.LISTING_VERSION;
import static com.github.tlb.TlbConstants.Server.REQUEST_NAMESPACE;

/**
 * @understands restlet tlb application for tlb server
 */
public class TlbApplication extends Application {

    public TlbApplication(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {
        Router router = new Router(getContext());

        router.attach(String.format("/{%s}/%s", REQUEST_NAMESPACE, EntryRepoFactory.SUBSET_SIZE), SubsetSizeResource.class);

        router.attach(String.format("/{%s}/%s", REQUEST_NAMESPACE, EntryRepoFactory.SUITE_RESULT), SuiteResultResource.class);

        router.attach(String.format("/{%s}/%s", REQUEST_NAMESPACE, EntryRepoFactory.SUITE_TIME), SuiteTimeResource.class);
        router.attach(String.format("/{%s}/%s/{%s}", REQUEST_NAMESPACE, EntryRepoFactory.SUITE_TIME, LISTING_VERSION), VersionedSuiteTimeResource.class);

        router.attach(String.format("/{%s}/%s", REQUEST_NAMESPACE, EntryRepoFactory.SMOOTHED_SUITE_TIME), SmoothingSuiteTimeResource.class);
        router.attach(String.format("/{%s}/%s/{%s}", REQUEST_NAMESPACE, EntryRepoFactory.SMOOTHED_SUITE_TIME, LISTING_VERSION), VersionedSmoothingSuiteTimeResource.class);

        return router;
    }
}
