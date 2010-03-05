package com.thoughtworks.cruise.tlb.splitter;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.file;
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
            protected List<TlbFileResource> subset(List<TlbFileResource> fileResources) {
                TlbFileResource foo = new JunitFileResource(new File("foo"));
                TlbFileResource bar = new JunitFileResource(new File("bar"));
                return Arrays.asList(foo, bar);
            }
        };
        criteria.talksToCruise(toCruise);
        List<TlbFileResource> resources = criteria.filter(new ArrayList<TlbFileResource>());

        assertThat(resources.size(), is(2));
        verify(toCruise).publishSubsetSize(2);
    }
}
