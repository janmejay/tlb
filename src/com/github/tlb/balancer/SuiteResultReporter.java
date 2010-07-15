package com.github.tlb.balancer;

import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.service.TalkToService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands posting the suite result entry to server
 */
public class SuiteResultReporter extends Resource {
    private static final Logger logger = Logger.getLogger(SuiteResultReporter.class.getName());

    protected TalkToService talkToService;

    public SuiteResultReporter(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        talkToService = (TalkToService) context.getAttributes().get(TlbClient.TALK_TO_SERVICE);
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        try {
            final SuiteResultEntry entry = SuiteResultEntry.parseSingleEntry(entity.getText());
            talkToService.testClassFailure(entry.getName(), entry.hasFailed());
        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("could not report test time: '%s'", e.getMessage()), e);
            throw new RuntimeException(e);
        }
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
