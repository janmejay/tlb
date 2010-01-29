package com.thoughtworks.cruise.tlb.splitter;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.core.Is.is;
import org.apache.tools.ant.types.resources.FileResource;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_STAGE_NAME;
import static com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_JOB_NAME;

import java.util.*;
import java.io.File;

import static junit.framework.Assert.fail;

public class CountBasedTestSplitterCriteriaTest {
    private TalkToCruise talkToCruise;

    @Before
    public void setUp() throws Exception {
        talkToCruise = mock(TalkToCruise.class);
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheFirstJob() {
        when(talkToCruise.getJobs("stage-1")).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = initEnvironment("job-1");

        FileResource first = file("first");
        FileResource second = file("second");
        List<FileResource> resources = Arrays.asList(first, second, file("third"), file("fourth"), file("fifth"));

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second)));
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheSecondJob() {
        when(talkToCruise.getJobs("stage-1")).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = initEnvironment("job-2");

        FileResource third = file("third");
        FileResource fourth = file("fourth");
        FileResource fifth = file("fifth");
        List<FileResource> resources = Arrays.asList(file("first"), file("second"), third, fourth, fifth);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(third, fourth, fifth)));
    }

    @Test
    public void shouldSplitTestsBalanced() {
        when(talkToCruise.getJobs("stage-1")).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<FileResource> resources = new ArrayList<FileResource>();

        for(int i = 0; i < 11; i++) {
            resources.add(new FileResource(new File("base" + i)));
        }

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(file("base0"), file("base1"), file("base2"))));
        criteria = new CountBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(file("base3"), file("base4"), file("base5"), file("base6"))));
        criteria = new CountBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-3"));
        assertThat(criteria.filter(resources), is(Arrays.asList(file("base7"),file("base8"), file("base9"), file("base10"))));
    }

    private FileResource file(String name) {
        return new FileResource(new File(name));
    }

    private SystemEnvironment initEnvironment(String jobName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(CRUISE_JOB_NAME, jobName);
        map.put(CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }
}
