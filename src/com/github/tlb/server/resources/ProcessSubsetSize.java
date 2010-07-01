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
public class ProcessSubsetSize extends Resource {
    private EntryRepo<SubsetSizeEntry> repo;

    public ProcessSubsetSize(Context context, Request request, Response response) {
        super(context, request, response);
        EntryRepoFactory repoFactory = (EntryRepoFactory) context.getAttributes().get(TlbConstants.Server.REPO_FACTORY);
        repo = repoFactory.getSubsetRepo((String) request.getAttributes().get(TlbConstants.Server.FAMILY_NAME));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        StringBuilder builder = new StringBuilder();
        for (SubsetSizeEntry subsetSizeEntry : repo.list()) {
            builder.append(subsetSizeEntry.dump()).append("\n");
        }
        return new StringRepresentation(builder.toString(), MediaType.TEXT_PLAIN);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        try {
            SubsetSizeEntry entry = SubsetSizeEntry.parse(entity.getText()).get(0);
            repo.add(entry);
        } catch (IOException e) {
            throw new IllegalArgumentException("Bad data");
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}
