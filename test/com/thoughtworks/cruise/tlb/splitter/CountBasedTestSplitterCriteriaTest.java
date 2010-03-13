package com.thoughtworks.cruise.tlb.splitter;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.core.Is.is;
import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.files;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.initEnvironment;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.file;

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
    public void shouldConsumeAllTestsWhenNoJobsToBalanceWith() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1"));

        SystemEnvironment env = initEnvironment("job-1");

        TlbFileResource first = file("first");
        TlbFileResource second = file("second");
        TlbFileResource third = file("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second, third)));
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheFirstJob() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = initEnvironment("job-1");

        TlbFileResource first = file("first");
        TlbFileResource second = file("second");
        List<TlbFileResource> resources = Arrays.asList(first, second, file("third"), file("fourth"), file("fifth"));

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second)));
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheSecondJob() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = initEnvironment("job-2");

        TlbFileResource third = file("third");
        TlbFileResource fourth = file("fourth");
        TlbFileResource fifth = file("fifth");
        List<TlbFileResource> resources = Arrays.asList(file("first"), file("second"), third, fourth, fifth);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(third, fourth, fifth)));
    }

    @Test
    public void shouldSplitTestsJobWithUUID() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-abcdef12-1234-3456-7890-abcdef123456", "job-e2345678-1234-3456-7890-abcdef123456"));

        SystemEnvironment env = initEnvironment("job-e2345678-1234-3456-7890-abcdef123456");

        TlbFileResource third = file("third");
        TlbFileResource fourth = file("fourth");
        TlbFileResource fifth = file("fifth");
        List<TlbFileResource> resources = Arrays.asList(file("first"), file("second"), third, fourth, fifth);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(third, fourth, fifth)));
    }

    @Test
    public void shouldSplitTestsBalanced() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 11; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2)));

        assertThat(criteria("job-2").filter(resources), is(files(3, 4, 5, 6)));

        assertThat(criteria("job-3").filter(resources), is(files(7, 8, 9, 10)));
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsAreMoreThanTests() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 2; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files()));
        assertThat(criteria("job-2").filter(resources), is(files(0)));
        assertThat(criteria("job-3").filter(resources), is(files(1)));
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsIsEqualToNumberOfTests() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 3; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0)));
        assertThat(criteria("job-2").filter(resources), is(files(1)));
        assertThat(criteria("job-3").filter(resources), is(files(2)));
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor37testsAcross7Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6", "job-7"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 37; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2, 3, 4))); //2/7

        assertThat(criteria("job-2").filter(resources), is(files(5, 6, 7, 8, 9))); //4/7

        assertThat(criteria("job-3").filter(resources), is(files(10, 11, 12, 13, 14))); //6/7

        assertThat(criteria("job-4").filter(resources), is(files(15, 16, 17, 18, 19, 20))); //1/7

        assertThat(criteria("job-5").filter(resources), is(files(21, 22, 23, 24, 25))); //3/7

        assertThat(criteria("job-6").filter(resources), is(files(26, 27, 28, 29, 30))); //5/7

        assertThat(criteria("job-7").filter(resources), is(files(31, 32, 33, 34, 35, 36))); //7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor41testsAcross7Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6", "job-7"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 41; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2, 3, 4))); //6/7

        assertThat(criteria("job-2").filter(resources), is(files(5, 6, 7, 8, 9, 10))); //12/7 = 5/7

        assertThat(criteria("job-3").filter(resources), is(files(11, 12, 13, 14, 15, 16))); //18/7 = 4/7

        assertThat(criteria("job-4").filter(resources), is(files(17, 18, 19, 20, 21, 22))); //24/7 = 3/7

        assertThat(criteria("job-5").filter(resources), is(files(23, 24, 25, 26, 27, 28))); //30/7 = 2/7

        assertThat(criteria("job-6").filter(resources), is(files(29, 30, 31, 32, 33, 34))); //36/7 = 1/7

        assertThat(criteria("job-7").filter(resources), is(files(35, 36, 37, 38, 39, 40))); //42/7 = 7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor36testsAcross6Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 36; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(files(0, 1, 2, 3, 4, 5)));

        assertThat(criteria("job-2").filter(resources), is(files(6, 7, 8, 9, 10, 11)));

        assertThat(criteria("job-3").filter(resources), is(files(12, 13, 14, 15, 16, 17)));

        assertThat(criteria("job-4").filter(resources), is(files(18, 19, 20, 21, 22, 23)));

        assertThat(criteria("job-5").filter(resources), is(files(24, 25, 26, 27, 28, 29)));

        assertThat(criteria("job-6").filter(resources), is(files(30, 31, 32, 33, 34, 35)));
    }


    private CountBasedTestSplitterCriteria criteria(String jobName) {
        return new CountBasedTestSplitterCriteria(talkToCruise, initEnvironment(jobName));
    }

}
