package com.thoughtworks.cruise.tlb.splitter;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import org.apache.tools.ant.types.resources.FileResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.hamcrest.core.Is;
import static org.hamcrest.core.Is.is;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TestUtil.file;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;

import java.util.*;
import java.io.File;

public class JobFamilyAwareSplitterCriteriaTest {
    @Test
    public void testFilterShouldPublishNumberOfSuitesSelectedForRunning() {
        HashMap<String, String> envMap = new HashMap<String, String>();
        envMap.put(TlbConstants.CRUISE_JOB_NAME, "build-1");
        TalkToCruise toCruise = mock(TalkToCruise.class);
        when(toCruise.getJobs()).thenReturn(Arrays.asList("build-1", "build-2", "build-3"));
        JobFamilyAwareSplitterCriteria criteria = new JobFamilyAwareSplitterCriteria(new SystemEnvironment(envMap)) {
            protected List<FileResource> subset(List<FileResource> fileResources) {
                return Arrays.asList(new FileResource(new File("foo")), new FileResource(new File("bar")));
            }
        };
        criteria.talksToCruise(toCruise);
        List<FileResource> resources = criteria.filter(new ArrayList<FileResource>());
        assertThat(resources.size(), is(2));
        verify(toCruise).publishSubsetSize(2);
    }
}
