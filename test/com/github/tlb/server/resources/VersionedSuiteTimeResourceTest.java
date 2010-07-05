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
import org.restlet.resource.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionedSuiteTimeResourceTest {
    private VersionedSuiteTimeResource suiteTimeResource;
    protected HashMap<String, Object> attributeMap;
    protected EntryRepoFactory factory;
    protected SuiteTimeRepo repo;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        Request request = mock(Request.class);
        factory = mock(EntryRepoFactory.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) factory));
        attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "namespace");
        when(request.getAttributes()).thenReturn(attributeMap);
        repo = mock(SuiteTimeRepo.class);
        when(factory.createSuiteTimeRepo("namespace", EntryRepoFactory.LATEST_VERSION)).thenReturn(repo);
        suiteTimeResource = new VersionedSuiteTimeResource(context, request, mock(Response.class));
    }

    @Test
    public void shouldAllowPostRequests() {
        assertThat(suiteTimeResource.allowPost(), is(false));
    }

    @Test
    public void shouldNotAllowPutRequests() {
        assertThat(suiteTimeResource.allowPut(), is(false));
    }

    @Test
    public void shouldUseSuiteTimeRepo() throws IOException, ClassNotFoundException {
        EntryRepo repo = suiteTimeResource.getRepo(factory, "namespace");
        assertThat((SuiteTimeRepo) repo, sameInstance(this.repo));
    }

    @Test
    public void shouldRenderAllRecordsForGivenNamespaceAndVersion() throws ResourceException, IOException, ClassNotFoundException {
        when(repo.list("foo")).thenReturn(Arrays.asList(new SuiteTimeEntry("foo.bar.Baz", 10), new SuiteTimeEntry("foo.bar.Quux", 20)));
        attributeMap.put(TlbConstants.Server.LISTING_VERSION, "foo");
        Representation actualRepresentation = suiteTimeResource.represent(new Variant(MediaType.TEXT_PLAIN));
        assertThat(actualRepresentation.getText(), is("foo.bar.Baz: 10\nfoo.bar.Quux: 20\n"));
        verify(repo).list("foo");
    }

}
