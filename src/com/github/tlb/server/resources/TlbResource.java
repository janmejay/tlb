package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
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
 * @understands listing and modification of tlb resource
 */
public class TlbResource extends Resource {
    private static final Logger logger = Logger.getLogger(EntryRepoFactory.class.getName());
    protected EntryRepo repo;

    public TlbResource(Context context, Request request, Response response) {
        super(context, request, response);
        EntryRepoFactory repoFactory = (EntryRepoFactory) context.getAttributes().get(TlbConstants.Server.REPO_FACTORY);
        String key = (String) request.getAttributes().get(TlbConstants.Server.REQUEST_NAMESPACE);
        try {
            repo = repoFactory.createSubsetRepo(key);
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("Failed to get repo for '%s'", key), e);
            throw new RuntimeException(e);
        }
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        StringBuilder builder = new StringBuilder();
        for (Object entry : repo.list()) {
            builder.append(entry).append("\n");
        }
        return new StringRepresentation(builder.toString(), MediaType.TEXT_PLAIN);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        try {
            repo.add(entity.getText());
        } catch (IOException e) {
            throw new IllegalArgumentException("Bad data");
        }
    }
}
