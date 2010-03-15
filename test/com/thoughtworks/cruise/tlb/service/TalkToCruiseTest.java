package com.thoughtworks.cruise.tlb.service;

import com.thoughtworks.cruise.tlb.TestUtil;
import static com.thoughtworks.cruise.tlb.TestUtil.fileContents;
import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.*;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.service.http.HttpAction;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.core.Is.is;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class TalkToCruiseTest {
    private TalkToCruise cruise;
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
    public void shouldLogWhenLoadingOrPersistingCachableData() throws Exception{
        SystemEnvironment env = initEnvironment("http://test.host:8153/cruise");
        FileUtil fileUtil = new FileUtil(env);

        try {
            FileUtils.forceDelete(fileUtil.getUniqueFile("foo"));
        } catch (IOException e) {
            //ignore, file may not be there!
        }
        TalkToCruise cruise = new TalkToCruise(env, mock(DefaultHttpAction.class));
        logFixture.startListening();
        cruise.persist("hello world\n", "foo");
        logFixture.assertHeard(String.format("Wrote [ hello world\n ] to %s [ identified by: foo ]", fileUtil.getUniqueFile("foo")));
        cruise.persist("hacking is fun\n", "foo");
        logFixture.assertHeard(String.format("Wrote [ hacking is fun\n ] to %s [ identified by: foo ]", fileUtil.getUniqueFile("foo")));
        cruise.persist("foo bar baz quux\n", "foo");
        logFixture.assertHeard(String.format("Wrote [ foo bar baz quux\n ] to %s [ identified by: foo ]", fileUtil.getUniqueFile("foo")));
        cruise.cache("foo");
        logFixture.assertHeard(String.format("Cached 3 lines from %s [ identified by: foo ], the last of which was [ foo bar baz quux ]", fileUtil.getUniqueFile("foo")));
    }

    @Test
    public void shouldReturnTheListOfJobsIntheGivenStage() throws Exception {
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        assertCanFindJobsFrom("http://test.host:8153/cruise", environment);
    }
    
    @Test
    public void shouldReturnSortedListOfPearJobs() throws Exception{
        Map<String, String> envMap = initEnvMap("http://test.host:8153/cruise");
        envMap.put(TlbConstants.CRUISE_JOB_NAME, "firefox-2");
        SystemEnvironment environment = new SystemEnvironment(envMap);
        HttpAction action = mock(HttpAction.class);

        when(action.get("http://test.host:8153/cruise/pipelines/pipeline/2/stage/1.xml")).thenReturn(TestUtil.fileContents("resources/stage_detail_with_jobs_in_random_order.xml"));
        stubJobDetails(action);

        cruise = new TalkToCruise(environment, action);
        assertThat(cruise.getJobs(), is(Arrays.asList("firefox-3", "rails", "firefox-1", "smoke", "firefox-2")));
        assertThat(cruise.pearJobs(), is(Arrays.asList("firefox-1", "firefox-2", "firefox-3")));
    }

    @Test
    public void shouldReturnSortedListOfPearJobsIdentifiedOnUUID() throws Exception{
        Map<String, String> envMap = initEnvMap("http://test.host:8153/cruise");
        envMap.put(TlbConstants.CRUISE_JOB_NAME, "firefox-bbcdef12-1234-1234-1234-abcdef123456");
        SystemEnvironment environment = new SystemEnvironment(envMap);
        HttpAction action = mock(HttpAction.class);

        when(action.get("http://test.host:8153/cruise/pipelines/pipeline/2/stage/1.xml")).thenReturn(TestUtil.fileContents("resources/stage_detail_with_jobs_in_random_order.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/140.xml")).thenReturn(TestUtil.fileContents("resources/job_details_140_UUID.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/139.xml")).thenReturn(TestUtil.fileContents("resources/job_details_139.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/141.xml")).thenReturn(TestUtil.fileContents("resources/job_details_141_UUID.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/142.xml")).thenReturn(TestUtil.fileContents("resources/job_details_142_UUID.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/143.xml")).thenReturn(TestUtil.fileContents("resources/job_details_143.xml"));

        cruise = new TalkToCruise(environment, action);
        assertThat(cruise.getJobs(), is(Arrays.asList("firefox-cbcdef12-1234-1234-1234-abcdef123456", "rails", "firefox-abcdef12-1234-1234-1234-abcdef123456", "smoke", "firefox-bbcdef12-1234-1234-1234-abcdef123456")));
        assertThat(cruise.pearJobs(), is(Arrays.asList("firefox-abcdef12-1234-1234-1234-abcdef123456", "firefox-bbcdef12-1234-1234-1234-abcdef123456", "firefox-cbcdef12-1234-1234-1234-abcdef123456")));
    }

    @Test
    public void shouldWorkWithUrlHavingPathWithTrailingSlash() throws Exception {
        SystemEnvironment environment = initEnvironment("https://test.host:8154/cruise/");
        assertCanFindJobsFrom("https://test.host:8154/cruise", environment);
    }

    @Test
    public void shouldUpdateCruiseArtifactWithTestTimeUsingPUT() throws Exception {
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        HttpAction action = mock(HttpAction.class);
        String data = "com.thoughtworks.tlb.TestSuite: 12\n";
        String url = "http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/" + TalkToCruise.TEST_TIME_FILE;

        TalkToCruise cruise = new TalkToCruise(environment, action);
        cruise.clearSuiteTimeCachingFile();
        cruise.persist("1\n", cruise.testSubsetSizeFileLocator);

        logFixture.startListening();
        cruise.testClassTime("com.thoughtworks.tlb.TestSuite", 12);
        logFixture.assertHeard("recording run time for suite com.thoughtworks.tlb.TestSuite");
        logFixture.assertHeard("Posting test run times for 1 suite to the cruise server.");
        verify(action).put(url, data);
    }

    @Test
    public void shouldAppendToSubsetSizeArtifactForMultipleCalls() throws Exception{
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        HttpAction action = mock(HttpAction.class);
        when(action.put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "10")).thenReturn("File tlb/subset_size was appended successfully");
        when(action.put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "20")).thenReturn("File tlb/subset_size was appended successfully");
        when(action.put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "25")).thenReturn("File tlb/subset_size was appended successfully");
        TalkToCruise toCruise = new TalkToCruise(environment, action);
        toCruise.clearSuiteTimeCachingFile();
        logFixture.startListening();
        toCruise.publishSubsetSize(10);
        logFixture.assertHeard("Posting balanced subset size as 10 to cruise server");
        List<String> times = new ArrayList<String>();
        times.add("10");
        assertThat(toCruise.cache(toCruise.testSubsetSizeFileLocator), is(times));
        toCruise.publishSubsetSize(20);
        logFixture.assertHeard("Posting balanced subset size as 20 to cruise server");
        times.add("20");
        assertThat(toCruise.cache(toCruise.testSubsetSizeFileLocator), is(times));
        toCruise.publishSubsetSize(25);
        logFixture.assertHeard("Posting balanced subset size as 25 to cruise server");
        times.add("25");
        assertThat(toCruise.cache(toCruise.testSubsetSizeFileLocator), is(times));
        verify(action).put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "10\n");
        verify(action).put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "20\n");
        verify(action).put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "25\n");
    }

    @Test
    public void shouldUpdateCruiseArtifactWithTestTimeUsingPUTOnlyOnTheLastSuite() throws Exception {
        SystemEnvironment env = initEnvironment("http://test.host:8153/cruise");
        FileUtil fileUtil = new FileUtil(env);
        HttpAction action = mock(HttpAction.class);
        String data = "com.thoughtworks.tlb.TestSuite: 12\n" +
                "com.thoughtworks.tlb.TestTimeBased: 15\n" +
                "com.thoughtworks.tlb.TestCountBased: 10\n" +
                "com.thoughtworks.tlb.TestCriteriaSelection: 30\n" +
                "com.thougthworks.tlb.SystemEnvTest: 8\n";
        String url = "http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/" + TalkToCruise.TEST_TIME_FILE;

        TalkToCruise cruise = new TalkToCruise(env, action);
        cruise.clearSuiteTimeCachingFile();
        cruise.persist("5\n", cruise.testSubsetSizeFileLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestSuite", 12);
        assertCacheState(env, 1, "com.thoughtworks.tlb.TestSuite: 12", cruise.jobLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestTimeBased", 15);
        assertCacheState(env, 2, "com.thoughtworks.tlb.TestTimeBased: 15", cruise.jobLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestCountBased", 10);
        assertCacheState(env, 3, "com.thoughtworks.tlb.TestCountBased: 10", cruise.jobLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestCriteriaSelection", 30);
        assertCacheState(env, 4, "com.thoughtworks.tlb.TestCriteriaSelection: 30", cruise.jobLocator);

        when(action.put(url, data)).thenReturn("File tlb/test_time.properties was appended successfully");


        cruise.testClassTime("com.thougthworks.tlb.SystemEnvTest", 8);
        assertThat(fileUtil.getUniqueFile(cruise.jobLocator).exists(), is(false));

        verify(action).put(url, data);
    }

    @Test
    public void shouldUpdateCruiseArtifactWithFailedTestListUsingPUTOnlyOnTheLastSuite() throws Exception {
        SystemEnvironment env = initEnvironment("http://test.host:8153/cruise");
        FileUtil fileUtil = new FileUtil(env);
        HttpAction action = mock(HttpAction.class);
        String data = "com.thoughtworks.tlb.FailedSuiteOne\n" +
                "com.thoughtworks.tlb.FailedSuiteTwo\n" +
                "com.thoughtworks.tlb.FailedSuiteThree\n";
        String url = "http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/" + TalkToCruise.FAILED_TESTS_FILE;

        TalkToCruise cruise = new TalkToCruise(env, action);
        cruise.clearSuiteTimeCachingFile();
        cruise.persist("3\n\10\n6\n", cruise.testSubsetSizeFileLocator);
        cruise.testClassFailure("com.thoughtworks.tlb.PassingSuite", false);
        assertCacheState(env, 1, "com.thoughtworks.tlb.PassingSuite: false", cruise.failedTestsListFileLocator);
        cruise.testClassFailure("com.thoughtworks.tlb.FailedSuiteOne", true);
        assertCacheState(env, 2, "com.thoughtworks.tlb.FailedSuiteOne: true", cruise.failedTestsListFileLocator);
        cruise.testClassFailure("com.thoughtworks.tlb.FailedSuiteTwo", true);
        assertCacheState(env, 3, "com.thoughtworks.tlb.FailedSuiteTwo: true", cruise.failedTestsListFileLocator);
        cruise.testClassFailure("com.thoughtworks.tlb.PassingSuiteTwo", false);
        assertCacheState(env, 4, "com.thoughtworks.tlb.PassingSuiteTwo: false", cruise.failedTestsListFileLocator);
        cruise.testClassFailure("com.thoughtworks.tlb.FailedSuiteThree", true);
        assertCacheState(env, 5, "com.thoughtworks.tlb.FailedSuiteThree: true", cruise.failedTestsListFileLocator);

        when(action.put(url, data)).thenReturn("File tlb/failed_tests was appended successfully");

        cruise.testClassFailure("com.thoughtworks.tlb.PassingSuiteThree", false);

        assertThat(fileUtil.getUniqueFile(cruise.failedTestsListFileLocator).exists(), is(true));
        assertThat(fileUtil.getUniqueFile(cruise.testSubsetSizeFileLocator).exists(), is(true));
        //should not clear files as test time post(which happens after this) needs it

        verify(action).put(url, data);
    }

    @Test
    public void shouldUpdateCruiseArtifactWithTestTimeUsingPUTOnlyOnTheLastSuiteAccordingToLastSubsetSizeEntry() throws Exception {
        SystemEnvironment env = initEnvironment("http://test.host:8153/cruise");
        FileUtil fileUtil = new FileUtil(env);
        HttpAction action = mock(HttpAction.class);
        String data = "com.thoughtworks.tlb.TestSuite: 12\n" +
                "com.thoughtworks.tlb.TestCriteriaSelection: 30\n" +
                "com.thougthworks.tlb.SystemEnvTest: 8\n";
        String url = "http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/" + TalkToCruise.TEST_TIME_FILE;

        TalkToCruise cruise = new TalkToCruise(env, action);
        cruise.clearSuiteTimeCachingFile();
        cruise.persist("5\n10\n3\n", cruise.testSubsetSizeFileLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestSuite", 12);
        assertCacheState(env, 1, "com.thoughtworks.tlb.TestSuite: 12", cruise.jobLocator);
        cruise.testClassTime("com.thoughtworks.tlb.TestCriteriaSelection", 30);
        assertCacheState(env, 2, "com.thoughtworks.tlb.TestCriteriaSelection: 30", cruise.jobLocator);

        when(action.put(url, data)).thenReturn("File tlb/test_time.properties was appended successfully");


        cruise.testClassTime("com.thougthworks.tlb.SystemEnvTest", 8);
        assertThat(fileUtil.getUniqueFile(cruise.jobLocator).exists(), is(false));

        verify(action).put(url, data);
    }

    private void assertCacheState(SystemEnvironment env, int lineCount, String lastLine, String locator) throws IOException {
        List<String> cache = cacheFileContents(env, locator);
        assertThat(cache.size(), is(lineCount));
        if (! cache.isEmpty()) {
            assertThat(cache.get(lineCount - 1), is(lastLine));
        }
    }

    private List cacheFileContents(SystemEnvironment env, String locator) throws IOException {
        FileUtil fileUtil = new FileUtil(env);
        File cacheFile = fileUtil.getUniqueFile(locator);
        if (! cacheFile.exists()) return new ArrayList();
        FileInputStream fileIn = new FileInputStream(cacheFile);
        List cachedLines = IOUtils.readLines(fileIn);
        IOUtils.closeQuietly(fileIn);
        return cachedLines;
    }

    @Test
    public void shouldPublishSubsetSizeAsALineAppendedToJobArtifact() throws Exception{
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        HttpAction action = mock(HttpAction.class);
        when(action.put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "10\n")).thenReturn("File tlb/subset_size was appended successfully");
        TalkToCruise toCruise = new TalkToCruise(environment, action);
        toCruise.publishSubsetSize(10);
        verify(action).put("http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/tlb/subset_size", "10\n");
    }

    @Test
    public void shouldFindFailedTestsFromTheLastRunStage() throws Exception{
        HttpAction action = mock(HttpAction.class);
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml")).thenReturn(TestUtil.fileContents("resources/stages_p1.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=60")).thenReturn(TestUtil.fileContents("resources/stages_p2.xml"));
        when(action.get("http://localhost:8153/cruise/api/stages/3.xml")).thenReturn(TestUtil.fileContents("resources/stage_detail.xml"));
        stubJobDetails(action);
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-1/tlb/failed_tests")).thenReturn(TestUtil.fileContents("resources/failed_tests_1"));
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-2/tlb/failed_tests")).thenReturn(TestUtil.fileContents("resources/failed_tests_2"));
        TalkToCruise cruise = new TalkToCruise(initEnvironment("http://localhost:8153/cruise"), action);
        List<String> failedTests = cruise.getLastRunFailedTests(Arrays.asList("firefox-1", "firefox-2"));
        Collections.sort(failedTests);
        assertThat(failedTests, is(Arrays.asList("com.thoughtworks.cruise.AnotherFailedTest", "com.thoughtworks.cruise.FailedTest", "com.thoughtworks.cruise.YetAnotherFailedTest")));
    }

    @Test
    public void shouldFindTestTimesFromLastRunStage() throws Exception{
        HttpAction action = mock(HttpAction.class);
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml")).thenReturn(fileContents("resources/stages_p1.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=60")).thenReturn(fileContents("resources/stages_p2.xml"));
        when(action.get("http://localhost:8153/cruise/api/stages/3.xml")).thenReturn(fileContents("resources/stage_detail.xml"));
        stubJobDetails(action);
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-1/tlb/test_time.properties")).thenReturn(fileContents("resources/test_time_1.properties"));
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-2/tlb/test_time.properties")).thenReturn(fileContents("resources/test_time_2.properties"));
        TalkToCruise cruise = new TalkToCruise(initEnvironment("http://localhost:8153/cruise"), action);
        Map<String, String> runTimes = cruise.getLastRunTestTimes(Arrays.asList("firefox-1", "firefox-2"));
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.thoughtworks.cruise.one.One", "10");
        map.put("com.thoughtworks.cruise.two.Two", "20");
        map.put("com.thoughtworks.cruise.three.Three", "30");
        map.put("com.thoughtworks.cruise.four.Four", "40");
        map.put("com.thoughtworks.cruise.five.Five", "50");
        assertThat(runTimes, is(map));
    }

    @Test
    public void shouldFindTestTimesFromLastRunStageWhenDeepDownFeedLinks() throws Exception{
        HttpAction action = mock(HttpAction.class);

        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml")).thenReturn(TestUtil.fileContents("resources/stages_p1.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=60")).thenReturn(TestUtil.fileContents("resources/stages_p2.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=42")).thenReturn(TestUtil.fileContents("resources/stages_p3.xml"));
        when(action.get("http://localhost:8153/cruise/api/stages/2.xml")).thenReturn(TestUtil.fileContents("resources/stage_detail.xml"));

        stubJobDetails(action);
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-1/tlb/test_time.properties")).thenReturn(fileContents("resources/test_time_1.properties"));
        when(action.get("http://localhost:8153/cruise/files/pipeline/1/stage/1/firefox-2/tlb/test_time.properties")).thenReturn(fileContents("resources/test_time_2_with_new_lines.properties"));
        Map<String, String> envMap = initEnvMap("http://localhost:8153/cruise");
        envMap.put(CRUISE_PIPELINE_NAME, "old_pipeline");
        envMap.put(CRUISE_STAGE_NAME, "old_stage");
        TalkToCruise cruise = new TalkToCruise(new SystemEnvironment(envMap), action);
        Map<String, String> runTimes = cruise.getLastRunTestTimes(Arrays.asList("firefox-1", "firefox-2"));
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.thoughtworks.cruise.one.One", "10");
        map.put("com.thoughtworks.cruise.two.Two", "20");
        map.put("com.thoughtworks.cruise.three.Three", "30");
        map.put("com.thoughtworks.cruise.four.Four", "40");
        map.put("com.thoughtworks.cruise.five.Five", "50");
        assertThat(runTimes, is(map));
    }

    private SystemEnvironment initEnvironment(String url) {
        return new SystemEnvironment(initEnvMap(url));
    }

    private Map<String, String> initEnvMap(String url) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(CRUISE_STAGE_NAME, "stage");
        map.put(TlbConstants.CRUISE_SERVER_URL, url);
        map.put(TlbConstants.CRUISE_PIPELINE_NAME, "pipeline");
        map.put(TlbConstants.CRUISE_PIPELINE_LABEL, "label-2");
        map.put(TlbConstants.CRUISE_JOB_NAME, "rspec");
        map.put(CRUISE_STAGE_NAME, "stage");
        map.put(CRUISE_STAGE_COUNTER, "1");
        map.put(CRUISE_PIPELINE_COUNTER, "2");
        map.put(TLB_TMP_DIR, System.getProperty("java.io.tmpdir"));
        return map;
    }

    private void assertCanFindJobsFrom(String baseUrl, SystemEnvironment environment) throws IOException, URISyntaxException {
        HttpAction action = mock(HttpAction.class);

        when(action.get(baseUrl + "/pipelines/pipeline/2/stage/1.xml")).thenReturn(fileContents("resources/stage_detail.xml"));
        stubJobDetails(action);

        cruise = new TalkToCruise(environment, action);
        logFixture.startListening();
        assertThat(cruise.getJobs(), is(Arrays.asList("firefox-1", "firefox-2", "firefox-3", "rails", "smoke")));
        logFixture.assertHeard("jobs found [firefox-1, firefox-2, firefox-3, rails, smoke]");
    }

    private void stubJobDetails(HttpAction action) throws IOException, URISyntaxException {
        when(action.get("http://test.host:8153/cruise/api/jobs/140.xml")).thenReturn(fileContents("resources/job_details_140.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/139.xml")).thenReturn(fileContents("resources/job_details_139.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/141.xml")).thenReturn(fileContents("resources/job_details_141.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/142.xml")).thenReturn(fileContents("resources/job_details_142.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/143.xml")).thenReturn(fileContents("resources/job_details_143.xml"));
    }
}
