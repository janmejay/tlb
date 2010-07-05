package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.Entry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import java.io.IOException;
import java.util.Collection;

/**
 * @understands versioned run time of suite reported by job
 */
public class VersionedSuiteTimeResource extends TlbResource {
    public VersionedSuiteTimeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws ClassNotFoundException, IOException {
        return repoFactory.createSuiteTimeRepo(key, EntryRepoFactory.LATEST_VERSION);
    }

    @Override
    protected Entry parseEntry(Representation entity) throws IOException {
        throw new UnsupportedOperationException("parsing does not make sense, as mutation of versioned data is not allowed");
    }

    @Override
    protected Collection getListing() throws IOException, ClassNotFoundException {
        return repo.list(strAttr(TlbConstants.Server.LISTING_VERSION));
    }
}