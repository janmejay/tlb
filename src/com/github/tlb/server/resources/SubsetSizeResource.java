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

/**
 * @understands subset sizes reported by a job
 */
public class SubsetSizeResource extends TlbResource {

    public SubsetSizeResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}
