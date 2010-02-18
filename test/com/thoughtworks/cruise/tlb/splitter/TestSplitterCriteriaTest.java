package com.thoughtworks.cruise.tlb.splitter;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import org.apache.tools.ant.types.resources.FileResource;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;

import static com.thoughtworks.cruise.tlb.TestUtil.file;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

public class TestSplitterCriteriaTest {

    @Before
    @After
    public void cleanUp() {
        System.clearProperty(TlbConstants.TEST_SUBSET_SIZE);
    }

    @Test
    public void shouldPublishSystemPropertyWithSubsetSize() throws Exception{
        final List<FileResource> resources = Arrays.asList(file("com/thoughtworks/cruise", "Foo"), file("com/thoughtworks/cruise", "Bar"), file("com/thoughtworks/cruise/domain", "Baz"));
        TalkToCruise talkToCruise = mock(TalkToCruise.class);
        when(talkToCruise.getJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        Map<String, String> env = new HashMap<String, String>();
        env.put(TlbConstants.CRUISE_JOB_NAME, "job-1");
        TestSplitterCriteria criteria = new JobFamilyAwareSplitterCriteria(talkToCruise, new SystemEnvironment(env)) {
            protected List<FileResource> subset(List<FileResource> fileResources) {
                return resources;
            }
        };

        criteria.filter(new ArrayList<FileResource>());
        assertThat(System.getProperty(TlbConstants.TEST_SUBSET_SIZE), is("3"));
    }
}
