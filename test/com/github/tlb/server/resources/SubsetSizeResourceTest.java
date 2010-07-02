package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.EntryRepoFactory;
import com.github.tlb.server.repo.SubsetSizeRepo;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubsetSizeResourceTest {
    private SubsetSizeResource subsetSizeResource;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        SubsetSizeRepo repo = mock(SubsetSizeRepo.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) repoFactory));
        Request request = mock(Request.class);
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "family_name");
        when(request.getAttributes()).thenReturn(attributeMap);
        when(repoFactory.createSubsetRepo(TlbConstants.Server.REQUEST_NAMESPACE)).thenReturn(repo);
        subsetSizeResource = new SubsetSizeResource(context, request, mock(Response.class));
    }

    @Test
    public void shouldAllowPostRequests() {
        assertThat(subsetSizeResource.allowPost(), is(true));
    }

    @Test
    public void shouldNotAllowPutRequests() {
        assertThat(subsetSizeResource.allowPut(), is(false));
    }

    @Test
    public void shouldUseSuiteTimeRepo() throws ClassNotFoundException, IOException {
        EntryRepoFactory repoFactory = mock(EntryRepoFactory.class);
        SubsetSizeRepo expectedRepo = mock(SubsetSizeRepo.class);
        when(repoFactory.createSubsetRepo("namespace")).thenReturn(expectedRepo);
        EntryRepo repo = subsetSizeResource.getRepo(repoFactory, "namespace");
        assertThat((SubsetSizeRepo) repo, sameInstance(expectedRepo));
    }
}