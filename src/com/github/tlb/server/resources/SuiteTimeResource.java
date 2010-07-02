package com.github.tlb.server.resources;

import com.github.tlb.server.EntryRepo;
import com.github.tlb.server.EntryRepoFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.IOException;

/**
 * @understands run time of suite reported by job
 */
public class SuiteTimeResource extends TlbResource {
    public SuiteTimeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws ClassNotFoundException, IOException {
        return repoFactory.createSuiteTimeRepo(key);
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}
