package com.github.tlb.splitter;

import com.github.tlb.*;
import com.github.tlb.service.TalkToCruise;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertThat;
import com.github.tlb.ant.JunitFileResource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;
import com.github.tlb.utils.SystemEnvironment;

import java.util.*;
import java.io.File;

public class JobFamilyAwareSplitterCriteriaTest {
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() {
        logFixture = new TestUtil.LogFixture();
    }

    @After
    public void tearDown() {
        logFixture.stopListening();
    }

    @Test
    public void testFilterShouldPublishNumberOfSuitesSelectedForRunning() {
        HashMap<String, String> envMap = new HashMap<String, String>();
        envMap.put(TlbConstants.Cruise.CRUISE_JOB_NAME, "build-1");
        TalkToCruise toCruise = mock(TalkToCruise.class);
        when(toCruise.totalPartitions()).thenReturn(3);

        JobFamilyAwareSplitterCriteria criteria = new JobFamilyAwareSplitterCriteria(new SystemEnvironment(envMap)) {
            protected List<TlbSuiteFile> subset(List<TlbSuiteFile> fileResources) {
                TlbSuiteFile foo = new TlbSuiteFileImpl("foo");
                TlbSuiteFile bar = new TlbSuiteFileImpl("bar");
                return Arrays.asList(foo, bar);
            }
        };
        criteria.talksToService(toCruise);
        logFixture.startListening();
        List<TlbFileResource> resources = criteria.filter(new ArrayList<TlbFileResource>());
        logFixture.assertHeard("got total of 0 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 3 ]");
        logFixture.assertHeard("assigned total of 2 files to [ build-1 ]");
        assertThat(resources.size(), is(2));
        verify(toCruise).publishSubsetSize(2);
    }
}
