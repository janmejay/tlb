package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.server.repo.EntryRepo;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.server.repo.SmoothingSuiteTimeRepo;
import com.github.tlb.server.repo.SuiteTimeRepo;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionedSmoothingSuiteTimeResourceTest {
    private VersionedSmoothingSuiteTimeResource suiteTimeResource;
    protected HashMap<String, Object> attributeMap;
    protected EntryRepoFactory factory;
    protected SmoothingSuiteTimeRepo repo;

    @Before
    public void setUp() throws ClassNotFoundException, IOException {
        Context context = new Context();
        Request request = mock(Request.class);
        factory = mock(EntryRepoFactory.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) factory));
        attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.REQUEST_NAMESPACE, "namespace");
        when(request.getAttributes()).thenReturn(attributeMap);
        repo = mock(SmoothingSuiteTimeRepo.class);
        when(factory.createSmoothingSuiteTimeRepo("namespace", EntryRepoFactory.LATEST_VERSION)).thenReturn(repo);
        suiteTimeResource = new VersionedSmoothingSuiteTimeResource(context, request, mock(Response.class));
    }

    @Test
    public void shouldUseSuiteTimeRepo() throws IOException, ClassNotFoundException {
        EntryRepo repo = suiteTimeResource.getRepo(factory, "namespace");
        assertThat((SmoothingSuiteTimeRepo) repo, sameInstance(this.repo));
    }
}