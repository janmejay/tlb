package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SuiteTimeRepo;
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
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionedResourceTest {
    private EntryRepoFactory factory;
    private Map<String, Object> attributeMap;
    private VersionedResource resource;
    protected SuiteTimeRepo mockRepo;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        Request request = mock(Request.class);
        factory = mock(EntryRepoFactory.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) factory));
        attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "namespace");
        when(request.getAttributes()).thenReturn(attributeMap);
        mockRepo = mock(SuiteTimeRepo.class);
        resource = new VersionedResource(context, request, mock(Response.class)) {
            @Override
            protected EntryRepo getRepo(EntryRepoFactory repoFactory, String key) throws IOException, ClassNotFoundException {
                return mockRepo;
            }
        };
    }

    @Test
    public void shouldAllowPostRequests() {
        assertThat(resource.allowPost(), is(false));
    }

    @Test
    public void shouldNotAllowPutRequests() {
        assertThat(resource.allowPut(), is(false));
    }

    @Test
    public void shouldNotSupportParsingOfEntryAsAddingToVersionedRepoIsNotPermitted() throws ResourceException, IOException {
        try {
            resource.parseEntry(new StringRepresentation("foo.bar.Baz: 120"));
            fail("should not have parsed entry, as mutation of versioned data is not allowed");
        } catch (Exception e) {
            assertThat(e, is(UnsupportedOperationException.class));
        }
    }

    @Test
    public void shouldRenderAllRecordsForGivenNamespaceAndVersion() throws ResourceException, IOException, ClassNotFoundException {
        when(mockRepo.list("foo")).thenReturn(Arrays.asList(new SuiteTimeEntry("foo.bar.Baz", 10), new SuiteTimeEntry("foo.bar.Quux", 20)));
        attributeMap.put(TlbConstants.Server.LISTING_VERSION, "foo");
        Representation actualRepresentation = resource.represent(new Variant(MediaType.TEXT_PLAIN));
        assertThat(actualRepresentation.getText(), is("foo.bar.Baz: 10\nfoo.bar.Quux: 20\n"));
        verify(mockRepo).list("foo");
    }
}
