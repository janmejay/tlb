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

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2)));

        assertThat(criteria("job-2").filter(resources), is(files(3, 4, 5, 6)));

        assertThat(criteria("job-3").filter(resources), is(files(7, 8, 9, 10)));
    }

    @Test//another to assertain it really works as expected
    public void shouldSplitTestsBalancedFor37testsAcross7Jobs() {
        when(talkToCruise.getJobs("stage-1")).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6", "job-7"));

        ArrayList<FileResource> resources = new ArrayList<FileResource>();

        for(int i = 0; i < 37; i++) {
            resources.add(new FileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2, 3, 4))); //2/7

        assertThat(criteria("job-2").filter(resources), is(files(5, 6, 7, 8, 9))); //4/7

        assertThat(criteria("job-3").filter(resources), is(files(10, 11, 12, 13, 14))); //6/7

        assertThat(criteria("job-4").filter(resources), is(files(15, 16, 17, 18, 19, 20))); //1/7

        assertThat(criteria("job-5").filter(resources), is(files(21, 22, 23, 24, 25))); //3/7

        assertThat(criteria("job-6").filter(resources), is(files(26, 27, 28, 29, 30))); //5/7

        assertThat(criteria("job-7").filter(resources), is(files(31, 32, 33, 34, 35, 36))); //7/7

    }

    private List<FileResource> files(int ... numbers) {
        ArrayList<FileResource> resources = new ArrayList<FileResource>();
        for (int number : numbers) {
            resources.add(file("base" + number));
        }
        return resources;
    }

    private CountBasedTestSplitterCriteria criteria(String jobName) {
        return new CountBasedTestSplitterCriteria(talkToCruise, initEnvironment(jobName));
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
