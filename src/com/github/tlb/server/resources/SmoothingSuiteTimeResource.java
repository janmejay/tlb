package com.github.tlb.server.resources;

import com.github.tlb.domain.Entry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import java.io.IOException;

/**
 * @understands smoothed run time of suite reported by jobs
 */
public class SmoothingSuiteTimeResource extends SuiteTimeResource {
    public SmoothingSuiteTimeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws ClassNotFoundException, IOException {
        return repoFactory.createSmoothingSuiteTimeRepo(key, EntryRepoFactory.LATEST_VERSION);
    }
}