package com.github.tlb.balancer;

import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.orderer.TestOrderer;
import com.github.tlb.splitter.TestSplitterCriteria;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands subseting and ordering of set of suite names given
 */
public class BalancerResource extends Resource {
    private static final Logger logger = Logger.getLogger(BalancerResource.class.getName());

    private final TestOrderer orderer;
    private final TestSplitterCriteria splitter;

    public BalancerResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        orderer = (TestOrderer) context.getAttributes().get(TlbClient.ORDERER);
        splitter = (TestSplitterCriteria) context.getAttributes().get(TlbClient.SPLITTER);
    }

    @Override
    public void acceptRepresentation(Representation representation) throws ResourceException {
        List<TlbSuiteFile> suiteFiles = null;
        try {
            suiteFiles = TlbSuiteFileImpl.parse(representation.getText());
        } catch (IOException e) {
            final String message = "failed to read request";
            logger.log(Level.WARNING, message, e);
            throw new RuntimeException(message, e);
        }
        final List<TlbSuiteFile> suiteFilesSubset = splitter.filterSuites(suiteFiles);
        Collections.sort(suiteFilesSubset, orderer);
        final StringBuilder builder = new StringBuilder();
        for (TlbSuiteFile suiteFile : suiteFilesSubset) {
            builder.append(suiteFile.dump());
        }
        getResponse().setEntity(new StringRepresentation(builder));
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return true;
    }
}
