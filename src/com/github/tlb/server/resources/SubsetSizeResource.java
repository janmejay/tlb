package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.server.EntryRepo;
import com.github.tlb.server.EntryRepoFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands subset sizes reported by a job
 */
public class SubsetSizeResource extends TlbResource {
    private static final Logger logger = Logger.getLogger(SubsetSizeResource.class.getName());

    public SubsetSizeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws IOException, ClassNotFoundException {
        return repoFactory.createSubsetRepo(key);
    }

    @Override
    public boolean allowPost() {
        return true;
    }
}
