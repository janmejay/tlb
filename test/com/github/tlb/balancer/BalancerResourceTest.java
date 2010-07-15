package com.github.tlb.balancer;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.orderer.TestOrderer;
import com.github.tlb.splitter.TestSplitterCriteria;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BalancerResourceTest {

    private BalancerResource balancerResource;
    private TestOrderer orderer;
    protected TestSplitterCriteria criteria;
    protected Request request;
    protected TestUtil.LogFixture logFixture;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        request = mock(Request.class);
        criteria = mock(TestSplitterCriteria.class);
        orderer = mock(TestOrderer.class);
        final HashMap<String, Object> ctxMap = new HashMap<String, Object>();
        ctxMap.put(TlbClient.SPLITTER, criteria);
        ctxMap.put(TlbClient.ORDERER, orderer);
        context.setAttributes(ctxMap);
        balancerResource = new BalancerResource(context, request, mock(Response.class));
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldListFilteredAndOrderedSubsetOfTlbTestSuitesProvided() throws ResourceException, IOException {
        when(request.getEntity()).thenReturn(new StringRepresentation("foo/bar/Baz.class\nfoo/bar/Bang.class\nfoo/bar/Quux.class\n"));

        when(criteria.filterSuites(new ArrayList<TlbSuiteFile>(Arrays.asList(new TlbSuiteFileImpl("foo/bar/Baz.class"), new TlbSuiteFileImpl("foo/bar/Bang.class"), new TlbSuiteFileImpl("foo/bar/Quux.class")))))
                .thenReturn(new ArrayList<TlbSuiteFile>(Arrays.asList(new TlbSuiteFileImpl("foo/bar/Baz.class"), new TlbSuiteFileImpl("foo/bar/Quux.class"))));
        when(orderer.compare(new TlbSuiteFileImpl("foo/bar/Baz.class"), new TlbSuiteFileImpl("foo/bar/Quux.class"))).thenReturn(1);

        Representation actualRepresentation = balancerResource.represent(new Variant(MediaType.TEXT_PLAIN));

        assertThat(actualRepresentation.getText(), is("foo/bar/Quux.class\nfoo/bar/Baz.class\n"));

        verify(criteria).filterSuites(criteria.filterSuites(new ArrayList<TlbSuiteFile>(Arrays.asList(new TlbSuiteFileImpl("foo/bar/Baz.class"), new TlbSuiteFileImpl("foo/bar/Bang.class"), new TlbSuiteFileImpl("foo/bar/Quux.class")))));
        verify(orderer).compare(new TlbSuiteFileImpl("foo/bar/Baz.class"), new TlbSuiteFileImpl("foo/bar/Quux.class"));
    }

    @Test
    public void shouldPropagateExceptionAndLogWhenFailsToGetRequestBody() throws ResourceException, IOException {
        logFixture.startListening();
        final IOException requestReadException = new IOException("test exception");
        final Representation representation = mock(Representation.class);
        when(representation.getText()).thenThrow(requestReadException);
        when(request.getEntity()).thenReturn(representation);
        try {
            balancerResource.represent(new Variant(MediaType.TEXT_PLAIN));
        } catch(RuntimeException e) {
            assertThat(e.getMessage(), is("failed to read request"));
            assertThat(e.getCause(), is((Throwable) requestReadException));
        }
        logFixture.stopListening();
        logFixture.assertHeard("failed to read request");
        logFixture.assertHeardException(requestReadException);
    }
    
    @Test
    public void shouldAcceptPlainText() {
        assertThat(balancerResource.getVariants().get(0).getMediaType(), is(MediaType.TEXT_PLAIN));
    }
}
