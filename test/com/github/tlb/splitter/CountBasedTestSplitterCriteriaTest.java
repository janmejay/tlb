package com.github.tlb.splitter;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.service.TalkToService;
import com.github.tlb.utils.SuiteFileConvertor;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.hamcrest.core.Is.is;
import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.utils.SystemEnvironment;

import java.util.*;
import java.io.File;

import static junit.framework.Assert.fail;

public class CountBasedTestSplitterCriteriaTest {
    private TalkToService talkToService;
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() throws Exception {
        talkToService = mock(TalkToCruise.class);
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldConsumeAllTestsWhenNoJobsToBalanceWith() {
        when(talkToService.totalPartitions()).thenReturn(1);
        when(talkToService.partitionNumber()).thenReturn(1);

        SystemEnvironment env = TestUtil.initEnvironment("job-1");

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        TlbFileResource third = TestUtil.junitFileResource("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToService, env);
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(first, second, third)));
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheFirstJob() {
        when(talkToService.totalPartitions()).thenReturn(2);
        when(talkToService.partitionNumber()).thenReturn(1);

        SystemEnvironment env = TestUtil.initEnvironment("job-1");

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        List<TlbFileResource> resources = Arrays.asList(first, second, TestUtil.junitFileResource("third"), TestUtil.junitFileResource("fourth"), TestUtil.junitFileResource("fifth"));

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToService, env);
        logFixture.startListening();
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(first, second)));
        logFixture.assertHeard("got total of 5 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("count balancing to approximately 2 files per job with 1 extra file to bucket");
        logFixture.assertHeard("assigned total of 2 files to [ job-1 ]");
    }

    @Test
    public void shouldSplitTestsBasedOnSplitFactorForTheSecondJob() {
        when(talkToService.totalPartitions()).thenReturn(2);
        when(talkToService.partitionNumber()).thenReturn(2);

        SystemEnvironment env = TestUtil.initEnvironment("job-2");

        TlbFileResource third = TestUtil.junitFileResource("third");
        TlbFileResource fourth = TestUtil.junitFileResource("fourth");
        TlbFileResource fifth = TestUtil.junitFileResource("fifth");
        List<TlbFileResource> resources = Arrays.asList(TestUtil.junitFileResource("first"), TestUtil.junitFileResource("second"), third, fourth, fifth);

        CountBasedTestSplitterCriteria criteria = new CountBasedTestSplitterCriteria(talkToService, env);
        logFixture.startListening();
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(third, fourth, fifth)));
        logFixture.assertHeard("got total of 5 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("count balancing to approximately 2 files per job with 1 extra file to bucket");
        logFixture.assertHeard("assigned total of 3 files to [ job-2 ]");
    }

    @Test
    public void shouldSplitTestsBalanced() {
        when(talkToService.totalPartitions()).thenReturn(3);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 11; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        logFixture.startListening();
        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources(0, 1, 2)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket");
        logFixture.assertHeard("assigned total of 3 files to [ job-1 ]");

        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(3, 4, 5, 6)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket", 2);
        logFixture.assertHeard("assigned total of 4 files to [ job-2 ]");

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(7, 8, 9, 10)));
        logFixture.assertHeard("count balancing to approximately 3 files per job with 2 extra file to bucket", 3);
        logFixture.assertHeard("assigned total of 4 files to [ job-3 ]");
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsAreMoreThanTests() {
        when(talkToService.totalPartitions()).thenReturn(3);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 2; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources()));
        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(0)));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(1)));
    }

    @Test
    public void shouldSplitTestsWhenTheSplitsIsEqualToNumberOfTests() {
        when(talkToService.totalPartitions()).thenReturn(3);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 3; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources(0)));
        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(1)));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(2)));
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor37testsAcross7Jobs() {
        when(talkToService.totalPartitions()).thenReturn(7);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 37; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        final SuiteFileConvertor convertor6 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles6 = convertor6.toTlbSuiteFiles(resources);
        assertThat(convertor6.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles6)), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4))); //2/7

        final SuiteFileConvertor convertor5 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles5 = convertor5.toTlbSuiteFiles(resources);
        assertThat(convertor5.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles5)), is(TestUtil.tlbFileResources(5, 6, 7, 8, 9))); //4/7

        final SuiteFileConvertor convertor4 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles4 = convertor4.toTlbSuiteFiles(resources);
        assertThat(convertor4.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles4)), is(TestUtil.tlbFileResources(10, 11, 12, 13, 14))); //6/7

        final SuiteFileConvertor convertor3 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles3 = convertor3.toTlbSuiteFiles(resources);
        assertThat(convertor3.toTlbFileResources(criteria("job-4", 4).filterSuites(suiteFiles3)), is(TestUtil.tlbFileResources(15, 16, 17, 18, 19, 20))); //1/7

        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-5", 5).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources(21, 22, 23, 24, 25))); //3/7

        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-6", 6).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(26, 27, 28, 29, 30))); //5/7

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-7", 7).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(31, 32, 33, 34, 35, 36))); //7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor41testsAcross7Jobs() {
        when(talkToService.totalPartitions()).thenReturn(7);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 41; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        final SuiteFileConvertor convertor6 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles6 = convertor6.toTlbSuiteFiles(resources);
        assertThat(convertor6.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles6)), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4))); //6/7

        final SuiteFileConvertor convertor5 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles5 = convertor5.toTlbSuiteFiles(resources);
        assertThat(convertor5.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles5)), is(TestUtil.tlbFileResources(5, 6, 7, 8, 9, 10))); //12/7 = 5/7

        final SuiteFileConvertor convertor4 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles4 = convertor4.toTlbSuiteFiles(resources);
        assertThat(convertor4.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles4)), is(TestUtil.tlbFileResources(11, 12, 13, 14, 15, 16))); //18/7 = 4/7

        final SuiteFileConvertor convertor3 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles3 = convertor3.toTlbSuiteFiles(resources);
        assertThat(convertor3.toTlbFileResources(criteria("job-4", 4).filterSuites(suiteFiles3)), is(TestUtil.tlbFileResources(17, 18, 19, 20, 21, 22))); //24/7 = 3/7

        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-5", 5).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources(23, 24, 25, 26, 27, 28))); //30/7 = 2/7

        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-6", 6).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(29, 30, 31, 32, 33, 34))); //36/7 = 1/7

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-7", 7).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(35, 36, 37, 38, 39, 40))); //42/7 = 7/7
    }

    @Test//to assertain it really works as expected
    public void shouldSplitTestsBalancedFor36testsAcross6Jobs() {
        when(talkToService.totalPartitions()).thenReturn(6);

        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();

        for(int i = 0; i < 36; i++) {
            resources.add(new JunitFileResource(new File("base" + i)));
        }

        final SuiteFileConvertor convertor5 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles5 = convertor5.toTlbSuiteFiles(resources);
        assertThat(convertor5.toTlbFileResources(criteria("job-1", 1).filterSuites(suiteFiles5)), is(TestUtil.tlbFileResources(0, 1, 2, 3, 4, 5)));

        final SuiteFileConvertor convertor4 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles4 = convertor4.toTlbSuiteFiles(resources);
        assertThat(convertor4.toTlbFileResources(criteria("job-2", 2).filterSuites(suiteFiles4)), is(TestUtil.tlbFileResources(6, 7, 8, 9, 10, 11)));

        final SuiteFileConvertor convertor3 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles3 = convertor3.toTlbSuiteFiles(resources);
        assertThat(convertor3.toTlbFileResources(criteria("job-3", 3).filterSuites(suiteFiles3)), is(TestUtil.tlbFileResources(12, 13, 14, 15, 16, 17)));

        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria("job-4", 4).filterSuites(suiteFiles2)), is(TestUtil.tlbFileResources(18, 19, 20, 21, 22, 23)));

        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria("job-5", 5).filterSuites(suiteFiles1)), is(TestUtil.tlbFileResources(24, 25, 26, 27, 28, 29)));

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria("job-6", 6).filterSuites(suiteFiles)), is(TestUtil.tlbFileResources(30, 31, 32, 33, 34, 35)));
    }

    private CountBasedTestSplitterCriteria criteria(String jobName, int partitionNumber) {
        when(talkToService.partitionNumber()).thenReturn(partitionNumber);
        return new CountBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment(jobName));
    }

}
