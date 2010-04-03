package com.github.tlb.splitter;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbFileResource;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.core.Is.is;
import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.utils.SystemEnvironment;

import java.util.*;
import java.io.File;

import static junit.framework.Assert.fail;

public class CountBasedTestSplitterCriteriaTest {
    private TalkToCruise talkToCruise;
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() throws Exception {
        talkToCruise = mock(TalkToCruise.class);
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldConsumeAllTestsWhenNoJobsToBalanceWith() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1"));

        SystemEnvironment env = TestUtil.initEnvironment("job-1");

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        TlbFileResource third = TestUtil.junitFileResource("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second, third)));
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheFirstJob() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = TestUtil.initEnvironment("job-1");

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        List<TlbFileResource> resources = Arrays.asList(first, second, TestUtil.junitFileResource("third"), TestUtil.junitFileResource("fourth"), TestUtil.junitFileResource("fifth"));

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        logFixture.startListening();
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second)));
        logFixture.assertHeard("got total of 5 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("count balancing to approximately 2 files per job with 1 extra file to bucket");
        logFixture.assertHeard("assigned total of 2 files to [ job-1 ]");
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheSecondJob() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));

        SystemEnvironment env = TestUtil.initEnvironment("job-2");

        TlbFileResource third = TestUtil.junitFileResource("third");
        TlbFileResource fourth = TestUtil.junitFileResource("fourth");
        TlbFileResource fifth = TestUtil.junitFileResource("fifth");
        List<TlbFileResource> resources = Arrays.asList(TestUtil.junitFileResource("first"), TestUtil.junitFileResource("second"), third, fourth, fifth);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToCruise, env);
        logFixture.startListening();
        assertThat(criteria.filter(resources), is(Arrays.asList(third, fourth, fifth)));
        logFixture.assertHeard("got total of 5 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("count balancing to approximately 2 files per job with 1 extra file to bucket");
        logFixture.assertHeard("assigned total of 3 files to [ job-2 ]");
    }

    @Test
    public void shouldSplitTestsJobWithUUID() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-abcdef12-1234-3456-7890-abcdef123456", "job-e2345678-1234-3456-7890-abcdef123456"));

        SystemEnvironment env = TestUtil.initEnvironment("job-e2345678-1234-3456-7890-abcdef123456");

        TlbFileResource third = TestUtil.junitFileResource("third");
        TlbFileResource fourth = TestUtil.junitFileResource("fourth");
        TlbFileResource fifth = TestUtil.junitFileResource("fifth");
        List<TlbFileResource> resources = Arrays.asList(TestUtil.junitFileResource("first"), TestUtil.junitFileResource("second"), third, fourth, fifth);

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

        logFixture.startListening();
        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources(0, 1, 2)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket");
        logFixture.assertHeard("assigned total of 3 files to [ job-1 ]");

        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(3, 4, 5, 6)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket", 2);
        logFixture.assertHeard("assigned total of 4 files to [ job-2 ]");

        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(7, 8, 9, 10)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket", 3);
        logFixture.assertHeard("assigned total of 4 files to [ job-3 ]");
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsAreMoreThanTests() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 2; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources()));
        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(0)));
        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(1)));
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsIsEqualToNumberOfTests() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 3; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources(0)));
        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(1)));
        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(2)));
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor37testsAcross7Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6", "job-7"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 37; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4))); //2/7

        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(5, 6, 7, 8, 9))); //4/7

        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(10, 11, 12, 13, 14))); //6/7

        assertThat(criteria("job-4").filter(resources), is(TestUtil.tlbFileResources(15, 16, 17, 18, 19, 20))); //1/7

        assertThat(criteria("job-5").filter(resources), is(TestUtil.tlbFileResources(21, 22, 23, 24, 25))); //3/7

        assertThat(criteria("job-6").filter(resources), is(TestUtil.tlbFileResources(26, 27, 28, 29, 30))); //5/7

        assertThat(criteria("job-7").filter(resources), is(TestUtil.tlbFileResources(31, 32, 33, 34, 35, 36))); //7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor41testsAcross7Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6", "job-7"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 41; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4))); //6/7

        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(5, 6, 7, 8, 9, 10))); //12/7 = 5/7

        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(11, 12, 13, 14, 15, 16))); //18/7 = 4/7

        assertThat(criteria("job-4").filter(resources), is(TestUtil.tlbFileResources(17, 18, 19, 20, 21, 22))); //24/7 = 3/7

        assertThat(criteria("job-5").filter(resources), is(TestUtil.tlbFileResources(23, 24, 25, 26, 27, 28))); //30/7 = 2/7

        assertThat(criteria("job-6").filter(resources), is(TestUtil.tlbFileResources(29, 30, 31, 32, 33, 34))); //36/7 = 1/7

        assertThat(criteria("job-7").filter(resources), is(TestUtil.tlbFileResources(35, 36, 37, 38, 39, 40))); //42/7 = 7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor36testsAcross6Jobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4", "job-5", "job-6"));

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 36; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        assertThat(criteria("job-1").filter(resources), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4, 5)));

        assertThat(criteria("job-2").filter(resources), is(TestUtil.tlbFileResources(6, 7, 8, 9, 10, 11)));

        assertThat(criteria("job-3").filter(resources), is(TestUtil.tlbFileResources(12, 13, 14, 15, 16, 17)));

        assertThat(criteria("job-4").filter(resources), is(TestUtil.tlbFileResources(18, 19, 20, 21, 22, 23)));

        assertThat(criteria("job-5").filter(resources), is(TestUtil.tlbFileResources(24, 25, 26, 27, 28, 29)));

        assertThat(criteria("job-6").filter(resources), is(TestUtil.tlbFileResources(30, 31, 32, 33, 34, 35)));
    }


    private CountBasedTestSplitterCriteria criteria(String jobName) {
        return new CountBasedTestSplitterCriteria(talkToCruise, TestUtil.initEnvironment(jobName));
    }

}
