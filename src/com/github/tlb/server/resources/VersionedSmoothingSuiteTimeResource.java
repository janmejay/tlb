package com.github.tlb.server.resources;

import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.IOException;

/**
 * @understands fetching and listing of smoothed suite time entries
 */
public class VersionedSmoothingSuiteTimeResource extends VersionedResource {
    public VersionedSmoothingSuiteTimeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws ClassNotFoundException, IOException {
        return repoFactory.createSmoothingSuiteTimeRepo(key, EntryRepoFactory.LATEST_VERSION);
    }
}
