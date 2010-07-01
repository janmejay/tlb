package com.github.tlb.server.resources;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SubsetSizeEntry;
import com.github.tlb.server.EntryRepo;
import com.github.tlb.server.EntryRepoFactory;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.apache.tools.ant.util.StringUtils;
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

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessSubsetSizeTest {
    private ProcessSubsetSize processSubsetSize;
    private Context context;
    private EntryRepoFactory repoFactory;
    private Request request;
    private HashMap<String,Object> attributeMap;
    private EntryRepo repo;

    @Before
    public void setUp() {
        context = new Context();
        repoFactory = mock(EntryRepoFactory.class);
        repo = mock(EntryRepo.class);
        context.setAttributes(Collections.singletonMap(TlbConstants.Server.REPO_FACTORY, (Object) repoFactory));
        request = mock(Request.class);
        attributeMap = new HashMap<String, Object>();
        attributeMap.put(TlbConstants.Server.FAMILY_NAME, "family_name");
        when(request.getAttributes()).thenReturn(attributeMap);
        when(repoFactory.getSubsetRepo(TlbConstants.Server.FAMILY_NAME)).thenReturn(repo);
        processSubsetSize = new ProcessSubsetSize(context, request, mock(Response.class));
    }

    @Test
    public void shouldRenderAllSubsetSizesForGivenJobName() throws ResourceException, IOException {
        when(repo.list()).thenReturn(Arrays.asList(new SubsetSizeEntry(10), new SubsetSizeEntry(12), new SubsetSizeEntry(15)));
        Representation actualRepresentation = processSubsetSize.represent(new Variant(MediaType.TEXT_PLAIN));
        assertThat(actualRepresentation.getText(), is("10\n12\n15\n"));
    }

    @Test
    public void shouldAddNewSubsets() throws ResourceException {
        StringRepresentation representation = new StringRepresentation("14");
        processSubsetSize.storeRepresentation(representation);
        verify(repo).add(new SubsetSizeEntry(14));
    }

    @Test
    public void shouldAllowPutRequests() {
        assertThat(processSubsetSize.allowPut(), is(true));
    }

    @Test
    public void shouldSupportTextPlain() {
        assertThat(processSubsetSize.getVariants().get(0).getMediaType(), is(MediaType.TEXT_PLAIN));
    }
}
