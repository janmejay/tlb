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
import com.thoughtworks.cruise.tlb.service.TalkToCruise;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;

public class JobFamilyAwareSplitterCriteriaTest {
    @Test
    public void testFilterShouldPublishNumberOfSuitesSelectedForRunning() {
        HashMap<String, String> envMap = new HashMap<String, String>();
        //envMap.put(TlbConstants.TEST_SUBSET_SIZE, "10");
//        envMap.put(TlbConstants.CRUISE_SERVER_URL, "http://localhost:8153/cruise");
//        envMap.put(TlbConstants.CRUISE_PIPELINE_NAME, "pipeline_one");
//        envMap.put(TlbConstants.CRUISE_STAGE_NAME, "stage_one");
//        envMap.put(TlbConstants.CRUISE_PIPELINE_COUNTER, "3");
//        envMap.put(TlbConstants.CRUISE_STAGE_COUNTER, "2");
//        envMap.put(TlbConstants.CRUISE_JOB_NAME, "build");
        envMap.put(TlbConstants.CRUISE_JOB_NAME, "build-1");
        TalkToCruise toCruise = mock(TalkToCruise.class);
        when(toCruise.getJobs()).thenReturn(Arrays.asList("build-1", "build-2", "build-3"));
        JobFamilyAwareSplitterCriteria criteria = new JobFamilyAwareSplitterCriteria(toCruise, new SystemEnvironment(envMap)) {
            protected List<FileResource> subset(List<FileResource> fileResources) {
                return Arrays.asList(new FileResource(new File("foo")), new FileResource(new File("bar")));
            }
        };
        List<FileResource> resources = criteria.filter(new ArrayList<FileResource>());
        assertThat(resources.size(), is(2));
        verify(toCruise).publishSubsetSize(2);
    }
}
