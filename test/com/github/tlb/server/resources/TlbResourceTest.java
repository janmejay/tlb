package com.github.tlb.server.resources;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import com.github.tlb.domain.Entry;
import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SubsetSizeRepo;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class TlbResourceTest {
    private TlbResource tlbResource;
    private Context context;
    private EntryRepoFactory repoFactory;
    private Request request;
    private HashMap<String,Object> attributeMap;
    private SubsetSizeRepo repo;
    private TestUtil.LogFixture logFixture;

    static class TestTlbResource extends TlbResource {
        public TestTlbResource(Context context, Request request, Response response) {
            super(context, request, response);
        }

        @Override
        protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws IOException, ClassNotFoundException {
            return repoFactory.createSubsetRepo(key, EntryRepoFactory.LATEST_VERSION);
        }

        @Override
        protected Entry parseEntry(Representation entity) throws IOException {
            return new SubsetSizeEntry(Integer.parseInt(entity.getText()));
        }
    }

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        context = new Context();
        repoFactory = mock(EntryRepoFactory.class);
        repo = mock(SubsetSizeRepo.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) repoFactory));
        request = mock(Request.class);
        attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "identifier");
        when(request.getAttributes()).thenReturn(attributeMap);
        when(repoFactory.createSubsetRepo("identifier", EntryRepoFactory.LATEST_VERSION)).thenReturn(repo);
        tlbResource = new TestTlbResource(context, request, mock(Response.class));
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldRenderAllRecordsForGivenNamespace() throws ResourceException, IOException {
        when(repo.list()).thenReturn(Arrays.asList(new SubsetSizeEntry(10), new SubsetSizeEntry(12), new SubsetSizeEntry(15)));
        Representation actualRepresentation = tlbResource.represent(new Variant(MediaType.TEXT_PLAIN));
        assertThat(actualRepresentation.getText(), is("10\n12\n15\n"));
        verify(repo).list();
    }
        
    @Test
    public void shouldThrowExceptionRaisedByRepoWhileListing() throws ResourceException, IOException, ClassNotFoundException {
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) final RuntimeException listingException = new RuntimeException("test exception");
        when(repo.list()).thenThrow(listingException);
        try {
            tlbResource.represent(new Variant(MediaType.TEXT_PLAIN));
            fail("list exception should have been propagated");
        } catch(Exception e) {
            assertThat((RuntimeException) e.getCause(), sameInstance(listingException));
        }
    }

    @Test
    public void shouldAddRecords() throws ResourceException {
        StringRepresentation representation = new StringRepresentation("14");
        tlbResource.acceptRepresentation(representation);
        verify(repo).add(new SubsetSizeEntry(14));
    }
    
    @Test
    public void shouldPropagateExceptionIfAddFails() throws ResourceException, IOException {
        Representation representation = mock(Representation.class);
        @SuppressWarnings({"ThrowableInstanceNeverThrown"}) IOException exception = new IOException("test exception");
        when(representation.getText()).thenThrow(exception);
        logFixture.startListening();
        try {
            tlbResource.acceptRepresentation(representation);
            fail("should have propagated exception");
        } catch (Exception e) {
            assertThat((IOException) e.getCause(), sameInstance(exception));
        }
        logFixture.assertHeardException(exception);
        logFixture.assertHeard("addition of representation failed for ");
        logFixture.stopListening();
    }

    @Test
    public void shouldPropagateExceptionIfUpdateFails() throws ResourceException, IOException {
        Representation representation = mock(Representation.class);
        IOException exception = new IOException("test exception");
        when(representation.getText()).thenThrow(exception);
        logFixture.startListening();
        try {
            tlbResource.storeRepresentation(representation);
            fail("should have propagated exception");
        } catch (Exception e) {
            assertThat((IOException) e.getCause(), sameInstance(exception));
        }
        logFixture.assertHeardException(exception);
        logFixture.assertHeard("update of representation failed for ");
        logFixture.stopListening();
    }

    @Test
    public void shouldUpdateRecords() throws ResourceException {
        StringRepresentation representation = new StringRepresentation("14");
        tlbResource.storeRepresentation(representation);
        verify(repo).update(new SubsetSizeEntry(14));
    }

    @Test
    public void shouldSupportTextPlain() {
        assertThat(tlbResource.getVariants().get(0).getMediaType(), is(MediaType.TEXT_PLAIN));
    }

    @Test
    public void shouldLogExceptionMessageIfFailsToGetRepo() throws ClassNotFoundException, IOException {
        repoFactory = mock(EntryRepoFactory.class);
        when(repoFactory.createSubsetRepo("identifier", EntryRepoFactory.LATEST_VERSION)).thenThrow(new IOException("test exception"));
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) repoFactory));
        logFixture.startListening();
        try {
            tlbResource = new TestTlbResource(context, request, mock(Response.class));
            fail("should have bubbled up exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("java.io.IOException: test exception"));
        }
        logFixture.stopListening();
        logFixture.assertHeardException(new IOException("test exception"));
        logFixture.assertHeard("Failed to get repo for 'identifier'");
    }
}
