package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SuiteResultRepo;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SuiteResultResourceTest {
    private SuiteResultResource suiteResultResource;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        Request request = mock(Request.class);
        EntryRepoFactory factory = mock(EntryRepoFactory.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) factory));
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "namespace");
        when(request.getAttributes()).thenReturn(attributeMap);
        suiteResultResource = new SuiteResultResource(context, request, mock(Response.class));
    }

    @Test
    public void shouldAllowPostRequests() {
        assertThat(suiteResultResource.allowPost(), is(false));
    }

    @Test
    public void shouldNotAllowPutRequests() {
        assertThat(suiteResultResource.allowPut(), is(true));
    }
    
    @Test
    public void shouldUseSuiteTimeRepo() throws IOException, ClassNotFoundException {
        EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        SuiteResultRepo expectedRepo = mock(SuiteResultRepo.class);
        when(repoFactory.createSuiteResultRepo("namespace", EntryRepoFactory.LATEST_VERSION)).thenReturn(expectedRepo);
        EntryRepo repo = suiteResultResource.getRepo(repoFactory, "namespace");
        assertThat((SuiteResultRepo)repo, sameInstance(expectedRepo));
    }

    @Test
    public void shouldParseSuitTimeEntry() throws IOException {
        final SuiteResultEntry entry = (SuiteResultEntry) suiteResultResource.parseEntry(new StringRepresentation("foo.bar.Baz: true"));
        assertThat(entry, is(new SuiteResultEntry("foo.bar.Baz", true)));
    }
}
