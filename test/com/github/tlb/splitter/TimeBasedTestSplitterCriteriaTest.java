package com.github.tlb.splitter;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbFileResource;
import com.github.tlb.domain.Entry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.service.TalkToService;
import com.github.tlb.service.TalkToCruise;
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
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1"));

        SystemEnvironment env = TestUtil.initEnvironment("job-1");

        TlbFileResource first = TestUtil.junitFileResource("first");
        TlbFileResource second = TestUtil.junitFileResource("second");
        TlbFileResource third = TestUtil.junitFileResource("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, env);
        logFixture.startListening();
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second, third)));
        logFixture.assertHeard("total jobs to distribute load [ 1 ]");
    }

    @Test
    public void shouldSplitTestsBasedOnTimeForTwoJob() {
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        List<SuiteTimeEntry> entries = testTimes();
        when(talkToService.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(entries);

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(second, first, third)));

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fourth, fifth)));
    }

    @Test
    public void shouldBombWhenNoTestTimeDataAvailable() {
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4"));
        when(talkToService.getLastRunTestTimes(Arrays.asList("job-1", "job-2", "job-3", "job-4"))).thenReturn(new ArrayList<SuiteTimeEntry>());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);


        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-3"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-4"));
        assertAbortsForNoHistoricalTimeData(resources, criteria);
    }

    private void assertAbortsForNoHistoricalTimeData(List<TlbFileResource> resources, TimeBasedTestSplitterCriteria criteria) {
        try {
            criteria.filter(resources);
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
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4"));
        when(talkToService.getLastRunTestTimes(Arrays.asList("job-1", "job-2", "job-3", "job-4"))).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(second)));

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fourth)));

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-3"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fifth)));

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-4"));
        assertThat(criteria.filter(resources), is(Arrays.asList(first, third)));
    }

    @Test
    public void shouldDistributeUnknownTestsBasedOnAverageTime() throws Exception{
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        when(talkToService.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");
        TlbFileResource fourth = TestUtil.tlbFileResource("foo/baz", "Fourth");
        TlbFileResource fifth = TestUtil.tlbFileResource("foo/bar", "Fourth");
        TlbFileResource firstNew = TestUtil.tlbFileResource("foo/quux", "First");
        TlbFileResource secondNew = TestUtil.tlbFileResource("foo/quux", "Second");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth, firstNew, secondNew);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();
        List<TlbFileResource> filteredResources = criteria.filter(resources);
        logFixture.assertHeard("got total of 7 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("historical test time data has entries for 5 suites");
        logFixture.assertHeard("5 entries of historical test time data found relavent");
        logFixture.assertHeard("encountered 2 new files which don't have historical time data, used average time [ 3.0 ] to balance");
        logFixture.assertHeard("assigned total of 4 files to [ job-1 ]");
        assertThat(filteredResources.size(), is(4));
        assertThat(filteredResources, hasItems(second, first, third, secondNew));

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));
        filteredResources = criteria.filter(resources);
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
        when(talkToService.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        when(talkToService.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(testTimes());

        TlbFileResource first = TestUtil.tlbFileResource("com/foo", "First");
        TlbFileResource second = TestUtil.tlbFileResource("com/foo", "Second");
        TlbFileResource third = TestUtil.tlbFileResource("com/bar", "Third");

        List<TlbFileResource> resources = Arrays.asList(second, first, third);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-1"));
        logFixture.startListening();

        assertThat(criteria.filter(resources), is(Arrays.asList(second)));
        logFixture.assertHeard("got total of 3 files to balance");
        logFixture.assertHeard("total jobs to distribute load [ 2 ]");
        logFixture.assertHeard("historical test time data has entries for 5 suites");
        logFixture.assertHeard("3 entries of historical test time data found relavent");
        logFixture.assertHeard("encountered 0 new files which don't have historical time data, used average time [ 3.0 ] to balance");
        logFixture.assertHeard("assigned total of 1 files to [ job-1 ]");

        criteria = new TimeBasedTestSplitterCriteria(talkToService, TestUtil.initEnvironment("job-2"));

        assertThat(criteria.filter(resources), is(Arrays.asList(first, third)));
        logFixture.assertHeard("got total of 3 files to balance", 2);
        logFixture.assertHeard("total jobs to distribute load [ 2 ]", 2);
        logFixture.assertHeard("historical test time data has entries for 5 suites", 2);
        logFixture.assertHeard("3 entries of historical test time data found relavent", 2);
        logFixture.assertHeard("encountered 0 new files which don't have historical time data, used average time [ 3.0 ] to balance", 2);
        logFixture.assertHeard("assigned total of 2 files to [ job-2 ]");
    }

    private List<SuiteTimeEntry> testTimes() {
        List<SuiteTimeEntry> entries = new ArrayList<SuiteTimeEntry>();
        entries.add(new SuiteTimeEntry("com.foo.First", 2l));
        entries.add(new SuiteTimeEntry("com.foo.Second", 5l));
        entries.add(new SuiteTimeEntry("com.bar.Third", 1l));
        entries.add(new SuiteTimeEntry("foo.baz.Fourth", 4l));
        entries.add(new SuiteTimeEntry("foo.bar.Fourth", 3l));
        return entries;
    }
}
