package com.thoughtworks.cruise.tlb.service;

import static com.thoughtworks.cruise.tlb.TlbConstants.*;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.service.http.HttpAction;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.apache.commons.io.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;

public class TalkToCruiseTest {

    @Before
    @After
    public void cleanUp() {
        System.clearProperty(TlbConstants.TEST_SUBSET_SIZE);
    }

    @Test
    public void shouldReturnTheListOfJobsIntheGivenStage() throws Exception {
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        assertCanFindJobsFrom("http://test.host:8153/cruise", environment);
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
        when(action.get("http://test.host:8153/cruise/properties/pipeline/label-2/stage/1/rspec/TEST_SUBSET_SIZE")).thenReturn("TEST_SUBSET_SIZE\n1");
        when(action.put(url, data)).thenReturn("File tlb.test_time.properties was appended successfully");

        TalkToCruise cruise = new TalkToCruise(environment, action);
        cruise.testClassTime("com.thoughtworks.tlb.TestSuite", 12);
        verify(action).put(url, data);
    }

    @Test
    public void shouldUpdateCruiseArtifactWithTestTimeUsingPUTOnlyOnTheLastSuite() throws Exception {
        SystemEnvironment environment = initEnvironment("http://test.host:8153/cruise");
        HttpAction action = mock(HttpAction.class);
        String data = "com.thoughtworks.tlb.TestSuite: 12\n" +
                "com.thoughtworks.tlb.TestTimeBased: 15\n" +
                "com.thoughtworks.tlb.TestCountBased: 10\n" +
                "com.thoughtworks.tlb.TestCriteriaSelection: 30\n" +
                "com.thougthworks.tlb.SystemEnvTest: 8\n";
        String url = "http://test.host:8153/cruise/files/pipeline/label-2/stage/1/rspec/" + TalkToCruise.TEST_TIME_FILE;

        when(action.get("http://test.host:8153/cruise/properties/pipeline/label-2/stage/1/rspec/TEST_SUBSET_SIZE")).thenReturn("TEST_SUBSET_SIZE\n5");
        TalkToCruise cruise = new TalkToCruise(environment, action);
        cruise.testClassTime("com.thoughtworks.tlb.TestSuite", 12);
        cruise.testClassTime("com.thoughtworks.tlb.TestTimeBased", 15);
        cruise.testClassTime("com.thoughtworks.tlb.TestCountBased", 10);
        cruise.testClassTime("com.thoughtworks.tlb.TestCriteriaSelection", 30);

        when(action.put(url, data)).thenReturn("File tlb.test_time.properties was appended successfully");

        cruise.testClassTime("com.thougthworks.tlb.SystemEnvTest", 8);

        verify(action).get("http://test.host:8153/cruise/properties/pipeline/label-2/stage/1/rspec/TEST_SUBSET_SIZE");
        verify(action).put(url, data);
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
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml")).thenReturn(fileContents("resources/stages_p1.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=60")).thenReturn(fileContents("resources/stages_p2.xml"));
        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=42")).thenReturn(fileContents("resources/stages_p3.xml"));
//        when(action.get("http://localhost:8153/cruise/api/feeds/stages.xml?before=37")).thenReturn(fileContents("resources/stages_p4.xml"));
        when(action.get("http://localhost:8153/cruise/api/stages/2.xml")).thenReturn(fileContents("resources/stage_detail.xml"));
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

    private String fileContents(String filePath) throws IOException, URISyntaxException {
        return FileUtils.readFileToString(new File(getClass().getClassLoader().getResource(filePath).toURI()));
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
        return map;
    }

    private void assertCanFindJobsFrom(String baseUrl, SystemEnvironment environment) throws IOException, URISyntaxException {
        HttpAction action = mock(HttpAction.class);

        when(action.get(baseUrl + "/pipelines/pipeline/2/stage/1.xml")).thenReturn(fileContents("resources/stage_detail.xml"));
        stubJobDetails(action);

        TalkToCruise cruise = new TalkToCruise(environment, action);
        assertThat(cruise.getJobs(), is(Arrays.asList("firefox-1", "firefox-2", "firefox-3", "rails", "smoke")));
    }

    private void stubJobDetails(HttpAction action) throws IOException, URISyntaxException {
        when(action.get("http://test.host:8153/cruise/api/jobs/140.xml")).thenReturn(fileContents("resources/job_details_140.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/139.xml")).thenReturn(fileContents("resources/job_details_139.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/141.xml")).thenReturn(fileContents("resources/job_details_141.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/142.xml")).thenReturn(fileContents("resources/job_details_142.xml"));
        when(action.get("http://test.host:8153/cruise/api/jobs/143.xml")).thenReturn(fileContents("resources/job_details_143.xml"));
    }
}
