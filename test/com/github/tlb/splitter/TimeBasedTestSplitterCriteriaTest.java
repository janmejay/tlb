package com.github.tlb.splitter;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.service.TalkToService;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.utils.SuiteFileConvertor;
import com.github.tlb.utils.SystemEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimeBasedTestSplitterCriteriaTest {

    private TalkToService talkToService;
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() throws Exception {
        talkToService = mock(TalkToCruise.class);
        logFixture = new TestUtil.LogFixture();
    }

    @After
    public void tearDown() {
        logFixture.stopListening();
    }

    @Test
    public void shouldConsumeAllTestsWhenNoJobsToBalanceWith() {
        when(talkToService.totalPartitions()).thenReturn(1);
        when(talkToService.partitionNumber()).thenReturn(1);

        SystemEnvironment env = new SystemEnvironment();

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        TlbFileResource third = TestUtil.junitFileResource("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, env);
        logFixture.startListening();
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(first, second, third)));
        logFixture.assertHeard("total jobs to distribute load [ 1 ]");
    }

    @Test
    public void shouldSplitTestsBasedOnTimeForTwoJob() {
        when(talkToService.totalPartitions()).thenReturn(2);

        List<SuiteTimeEntry> entries = testTimes();
        when(talkToService.getLastRunTestTimes()).thenReturn(entries);

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);
        when(talkToService.partitionNumber()).thenReturn(1);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria.filterSuites(suiteFiles1)), is(Arrays.asList(second, first, third)));

        when(talkToService.partitionNumber()).thenReturn(2);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(fourth, fifth)));
    }

    @Test
    public void shouldBombWhenNoTestTimeDataAvailable() {
        when(talkToService.totalPartitions()).thenReturn(4);

        when(talkToService.getLastRunTestTimes()).thenReturn(new ArrayList<SuiteTimeEntry>());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        when(talkToService.partitionNumber()).thenReturn(1);
        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        when(talkToService.partitionNumber()).thenReturn(2);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        when(talkToService.partitionNumber()).thenReturn(3);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-3"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        when(talkToService.partitionNumber()).thenReturn(4);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-4"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);
    }

    private void assertAbortsForNoHistoricalTimeData(List<TlbFileResource> resources, TimeBasedTestSplitterCriteria criteria) {
        try {
            final SuiteFileConvertor convertor = new SuiteFileConvertor();
            final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
            convertor.toTlbFileResources(criteria.filterSuites(suiteFiles));
            fail("should have aborted, as no historical test time data was given");
        } catch (Exception e) {
            String message = "no historical test time data, aborting attempt to balance based on time";
            logFixture.assertHeard(message);
            logFixture.clearHistory();
            assertThat(e.getMessage(), is(message));
        }
    }

    @Test
    public void shouldSplitTestsBasedOnTimeForFourJobs() {
        when(talkToService.totalPartitions()).thenReturn(4);
        when(talkToService.getLastRunTestTimes()).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        when(talkToService.partitionNumber()).thenReturn(1);
        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        final SuiteFileConvertor convertor3 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles3 = convertor3.toTlbSuiteFiles(resources);
        assertThat(convertor3.toTlbFileResources(criteria.filterSuites(suiteFiles3)), is(Arrays.asList(second)));

        when(talkToService.partitionNumber()).thenReturn(2);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        final SuiteFileConvertor convertor2 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles2 = convertor2.toTlbSuiteFiles(resources);
        assertThat(convertor2.toTlbFileResources(criteria.filterSuites(suiteFiles2)), is(Arrays.asList(fourth)));

        when(talkToService.partitionNumber()).thenReturn(3);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-3"));
        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria.filterSuites(suiteFiles1)), is(Arrays.asList(fifth)));

        when(talkToService.partitionNumber()).thenReturn(4);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-4"));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(first, third)));
    }

    @Test
    public void shouldDistributeUnknownTestsBasedOnAverageTime() throws Exception{
        when(talkToService.totalPartitions()).thenReturn(2);
        when(talkToService.getLastRunTestTimes()).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        TlbFileResource firstNew = TestUtil.tlbFileResource("foo/quux", "First");
        TlbFileResource secondNew = TestUtil.tlbFileResource("foo/quux", "Second");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth, firstNew, secondNew);

        when(talkToService.partitionNumber()).thenReturn(1);
        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();
        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        List<TlbFileResource> filteredResources = convertor1.toTlbFileResources(criteria.filterSuites(suiteFiles1));
        logFixture.assertHeard("got total of 7 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("historical test time data has entries for 5 suites");
        logFixture.assertHeard("5 entries of historical test time data found relavent");
        logFixture.assertHeard("encountered 2 new files which don't have historical time data, used average time [ 3.0 ] to balance");
        logFixture.assertHeard("assigned total of 4 files to [ job-1 ]");
        assertThat(filteredResources.size(), is(4));
        assertThat(filteredResources, hasItems(second, first, third, secondNew));

        when(talkToService.partitionNumber()).thenReturn(2);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        filteredResources = convertor.toTlbFileResources(criteria.filterSuites(suiteFiles));
        logFixture.assertHeard("got total of 7 files to balance", 2);
        logFixture.assertHeard("total jobs to distribute load [ 2 ]", 2);
        logFixture.assertHeard("historical test time data has entries for 5 suites", 2);
        logFixture.assertHeard("5 entries of historical test time data found relavent", 2);
        logFixture.assertHeard("encountered 2 new files which don't have historical time data, used average time [ 3.0 ] to balance", 2);
        logFixture.assertHeard("assigned total of 3 files to [ job-2 ]");
        assertThat(filteredResources.size(), is(3));
        assertThat(filteredResources, hasItems(fourth, fifth, firstNew));
    }

    @Test
    public void shouldIgnoreDeletedTests() throws Exception{
        when(talkToService.totalPartitions()).thenReturn(2);
        when(talkToService.getLastRunTestTimes()).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");

        List<TlbFileResource> resources = Arrays.asList(second, first, third);

        when(talkToService.partitionNumber()).thenReturn(1);
        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();

        final SuiteFileConvertor convertor1 = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles1 = convertor1.toTlbSuiteFiles(resources);
        assertThat(convertor1.toTlbFileResources(criteria.filterSuites(suiteFiles1)), is(Arrays.asList(second)));
        logFixture.assertHeard("got total of 3 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("historical test time data has entries for 5 suites");
        logFixture.assertHeard("3 entries of historical test time data found relavent");
        logFixture.assertHeard("encountered 0 new files which don't have historical time data, used average time [ 3.0 ] to balance");
        logFixture.assertHeard("assigned total of 1 files to [ job-1 ]");

        when(talkToService.partitionNumber()).thenReturn(2);
        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(convertor.toTlbFileResources(criteria.filterSuites(suiteFiles)), is(Arrays.asList(first, third)));
        logFixture.assertHeard("got total of 3 files to balance", 2);
        logFixture.assertHeard("total jobs to distribute load [ 2 ]", 2);
        logFixture.assertHeard("historical test time data has entries for 5 suites", 2);
        logFixture.assertHeard("3 entries of historical test time data found relavent", 2);
        logFixture.assertHeard("encountered 0 new files which don't have historical time data, used average time [ 3.0 ] to balance", 2);
        logFixture.assertHeard("assigned total of 2 files to [ job-2 ]");
    }

    private List<SuiteTimeEntry> testTimes() {
        List<SuiteTimeEntry> entries = new ArrayList<SuiteTimeEntry>();
        entries.add(new SuiteTimeEntry("com/foo/First.class", 2l));
        entries.add(new SuiteTimeEntry("com/foo/Second.class", 5l));
        entries.add(new SuiteTimeEntry("com/bar/Third.class", 1l));
        entries.add(new SuiteTimeEntry("foo/baz/Fourth.class", 4l));
        entries.add(new SuiteTimeEntry("foo/bar/Fourth.class", 3l));
        return entries;
    }
}
