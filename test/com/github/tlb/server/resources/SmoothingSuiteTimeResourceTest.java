package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SuiteTimeRepo;
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

public class SmoothingSuiteTimeResourceTest {
    private SuiteTimeResource suiteTimeResource;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        Request request = mock(Request.class);
        EntryRepoFactory factory = mock(EntryRepoFactory.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) factory));
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "namespace");
        when(request.getAttributes()).thenReturn(attributeMap);
        suiteTimeResource = new SmoothingSuiteTimeResource(context, request, mock(Response.class));
    }

    @Test
    public void shouldUseSmoothingSuiteTimeRepo() throws IOException, ClassNotFoundException {
        EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        SuiteTimeRepo expectedRepo = mock(SuiteTimeRepo.class);
        when(repoFactory.createSmoothingSuiteTimeRepo("namespace", EntryRepoFactory.LATEST_VERSION)).thenReturn(expectedRepo);
        EntryRepo repo = suiteTimeResource.getRepo(repoFactory, "namespace");
        assertThat((SuiteTimeRepo)repo, sameInstance(expectedRepo));
    }
}