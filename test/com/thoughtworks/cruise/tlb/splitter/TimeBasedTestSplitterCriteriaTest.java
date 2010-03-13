package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.initEnvironment;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.file;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import org.junit.Before;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class TimeBasedTestSplitterCriteriaTest {

    private TalkToCruise talkToCruise;

    @Before
    public void setUp() throws Exception {
        talkToCruise = mock(TalkToCruise.class);
    }

    @Test
    public void shouldConsumeAllTestsWhenNoJobsToBalanceWith() {
        when(talkToCruise.getJobs()).thenReturn(Arrays.asList("job-1", "foo", "bar"));

        SystemEnvironment env = initEnvironment("job-1");

        TlbFileResource first = file("first");
        TlbFileResource second = file("second");
        TlbFileResource third = file("third");
        List<TlbFileResource> resources = Arrays.asList(first, second, third);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToCruise, env);
        assertThat(criteria.filter(resources), is(Arrays.asList(first, second, third)));
    }

    @Test
    public void shouldSplitTestsBasedOnTimeForTwoJob() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        HashMap<String, String> map = testTimes();
        when(talkToCruise.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(map);

        TlbFileResource first = file("com/foo", "First");
        TlbFileResource second = file("com/foo", "Second");
        TlbFileResource third = file("com/bar", "Third");
        TlbFileResource fourth = file("foo/baz", "Fourth");
        TlbFileResource fifth = file("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(second, first, third)));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fourth, fifth)));
    }

    @Test
    public void shouldSplitTestsBasedOnTimeForFourJobs() {
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3", "job-4"));
        HashMap<String, String> map = testTimes();
        when(talkToCruise.getLastRunTestTimes(Arrays.asList("job-1", "job-2", "job-3", "job-4"))).thenReturn(map);

        TlbFileResource first = file("com/foo", "First");
        TlbFileResource second = file("com/foo", "Second");
        TlbFileResource third = file("com/bar", "Third");
        TlbFileResource fourth = file("foo/baz", "Fourth");
        TlbFileResource fifth = file("foo/bar", "Fourth");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(second)));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fourth)));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-3"));
        assertThat(criteria.filter(resources), is(Arrays.asList(fifth)));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-4"));
        assertThat(criteria.filter(resources), is(Arrays.asList(first, third)));
    }

    @Test
    public void shouldDistributeUnknownTestsBasedOnAverageTime() throws Exception{
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        HashMap<String, String> map = testTimes();
        when(talkToCruise.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(map);

        TlbFileResource first = file("com/foo", "First");
        TlbFileResource second = file("com/foo", "Second");
        TlbFileResource third = file("com/bar", "Third");
        TlbFileResource fourth = file("foo/baz", "Fourth");
        TlbFileResource fifth = file("foo/bar", "Fourth");
        TlbFileResource firstNew = file("foo/quux", "First");
        TlbFileResource secondNew = file("foo/quux", "Second");
        List<TlbFileResource> resources = Arrays.asList(first, second, third, fourth, fifth, firstNew, secondNew);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-1"));
        List<TlbFileResource> filteredResources = criteria.filter(resources);
        assertThat(filteredResources.size(), is(4));
        assertThat(filteredResources, hasItems(second, first, third, secondNew));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-2"));
        filteredResources = criteria.filter(resources);
        assertThat(filteredResources.size(), is(3));
        assertThat(filteredResources, hasItems(fourth, fifth, firstNew));
    }

    @Test
    public void shouldIgnoreDeletedTests() throws Exception{
        when(talkToCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2"));
        HashMap<String, String> map = testTimes();
        when(talkToCruise.getLastRunTestTimes(Arrays.asList("job-1", "job-2"))).thenReturn(map);

        TlbFileResource first = file("com/foo", "First");
        TlbFileResource second = file("com/foo", "Second");
        TlbFileResource third = file("com/bar", "Third");

        List<TlbFileResource> resources = Arrays.asList(second, first, third);

        TimeBasedTestSplitterCriteria criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-1"));
        assertThat(criteria.filter(resources), is(Arrays.asList(second)));

        criteria = new TimeBasedTestSplitterCriteria(talkToCruise, initEnvironment("job-2"));
        assertThat(criteria.filter(resources), is(Arrays.asList(first, third)));
    }

    private HashMap<String, String> testTimes() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("com.foo.First", "2");
        map.put("com.foo.Second", "5");
        map.put("com.bar.Third", "1");
        map.put("foo.baz.Fourth", "4");
        map.put("foo.bar.Fourth", "3");
        return map;
    }
}
