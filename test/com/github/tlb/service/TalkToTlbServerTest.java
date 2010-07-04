package com.github.tlb.service;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.Main;
import com.github.tlb.service.http.DefaultHttpAction;
import com.github.tlb.utils.SystemEnvironment;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Component;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * This in addition to being a talk-to-tlb-server test is also a tlb-server integration test,
 * hence uses real instance of tlb server
 */
public class TalkToTlbServerTest {
    private static Component component;
    private TalkToTlbServer talkToTlb;
    private static String freePort;
    private HashMap<String,String> clientEnv;
    private DefaultHttpAction httpAction;

    @BeforeClass
    public static void startTlbServer() throws Exception {
        HashMap<String, String> serverEnv = new HashMap<String, String>();
        freePort = TestUtil.findFreePort();
        serverEnv.put(TlbConstants.Server.TLB_PORT, freePort);
        serverEnv.put(TlbConstants.Server.TLB_STORE_DIR, TestUtil.createTempFolder().getAbsolutePath());
        Main main = new Main(new SystemEnvironment(serverEnv));
        component = main.init();
        component.start();
    }

    @AfterClass
    public static void shutDownTlbServer() throws Exception {
        component.stop();
    }

    @Before
    public void setUp() throws URIException {
        clientEnv = new HashMap<String, String>();
        clientEnv.put(TlbConstants.TlbServer.JOB_NAMESPACE, "job");
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "4");
        clientEnv.put(TlbConstants.TlbServer.TOTAL_PARTITIONS, "15");
        String url = "http://localhost:" + freePort;
        clientEnv.put(TlbConstants.TlbServer.URL, url);
        HttpClientParams params = new HttpClientParams();
        httpAction = new DefaultHttpAction(new HttpClient(params), new URI(url, true));
        talkToTlb = new TalkToTlbServer(new SystemEnvironment(clientEnv), httpAction);
    }

    @Test
    public void shouldBeAbleToPostSubsetSize() {
        talkToTlb.publishSubsetSize(10);
        talkToTlb.publishSubsetSize(20);
        talkToTlb.publishSubsetSize(17);
        assertThat(httpAction.get(String.format("http://localhost:%s/job-4/subset_size", freePort)), is("10\n20\n17\n"));
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "5");
        talkToTlb.publishSubsetSize(12);
        talkToTlb.publishSubsetSize(13);
        assertThat(httpAction.get(String.format("http://localhost:%s/job-5/subset_size", freePort)), is("12\n13\n"));
    }

    @Test
    public void shouldBeAbleToPostSuiteTime() {
        talkToTlb.testClassTime("com.foo.Foo", 100);
        talkToTlb.testClassTime("com.bar.Bar", 120);
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "2");
        talkToTlb.testClassTime("com.baz.Baz", 15);
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "15");
        talkToTlb.testClassTime("com.quux.Quux", 137);
        final String response = httpAction.get(String.format("http://localhost:%s/job/suite_time", freePort));
        final List<SuiteTimeEntry> entryList = SuiteTimeEntry.parse(response);
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.foo.Foo", 100)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.bar.Bar", 120)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.baz.Baz", 15)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.quux.Quux", 137)));
    }
    
    @Test
    public void shouldBeAbleToPostSuiteResult() {
        talkToTlb.testClassFailure("com.foo.Foo", true);
        talkToTlb.testClassFailure("com.bar.Bar", false);
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "2");
        talkToTlb.testClassFailure("com.baz.Baz", true);
        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "15");
        talkToTlb.testClassFailure("com.quux.Quux", true);
        final String response = httpAction.get(String.format("http://localhost:%s/job/suite_result", freePort));
        final List<SuiteResultEntry> entryList = SuiteResultEntry.parse(response);
        System.out.println("entryList = " + entryList);
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.foo.Foo", true)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.bar.Bar", false)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.baz.Baz", true)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.quux.Quux", true)));
    }

    @Test
    public void shouldBeAbleToFetchSuiteTimes() {
        final String url = String.format("http://localhost:%s/job/suite_time", freePort);
        httpAction.put(url, "com.foo.Foo: 10");
        httpAction.put(url, "com.bar.Bar: 12");
        httpAction.put(url, "com.baz.Baz: 17");
        httpAction.put(url, "com.quux.Quux: 150");

        List<SuiteTimeEntry> entryList = talkToTlb.getLastRunTestTimes();
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.foo.Foo", 10)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.bar.Bar", 12)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.baz.Baz", 17)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.quux.Quux", 150)));

        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "2");
        entryList = talkToTlb.getLastRunTestTimes();
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.foo.Foo", 10)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.bar.Bar", 12)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.baz.Baz", 17)));
        assertThat(entryList, hasItem(new SuiteTimeEntry("com.quux.Quux", 150)));
    }

    @Test
    public void shouldBeAbleToFetchSuiteResults() {
        final String url = String.format("http://localhost:%s/job/suite_result", freePort);
        httpAction.put(url, "com.foo.Foo: true");
        httpAction.put(url, "com.bar.Bar: false");
        httpAction.put(url, "com.baz.Baz: false");
        httpAction.put(url, "com.quux.Quux: true");

        List<SuiteResultEntry> entryList = talkToTlb.getLastRunFailedTests();
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.foo.Foo", true)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.bar.Bar", false)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.baz.Baz", false)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.quux.Quux", true)));

        clientEnv.put(TlbConstants.TlbServer.PARTITION_NUMBER, "2");
        entryList = talkToTlb.getLastRunFailedTests();
        assertThat(entryList.size(), is(4));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.foo.Foo", true)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.bar.Bar", false)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.baz.Baz", false)));
        assertThat(entryList, hasItem(new SuiteResultEntry("com.quux.Quux", true)));
    }
    
    @Test
    public void shouldReadTotalPartitionsFromEnvironmentVariables() {
        assertThat(talkToTlb.totalPartitions(), is(15));
        clientEnv.put(TlbConstants.TlbServer.TOTAL_PARTITIONS, "7");
        assertThat(talkToTlb.totalPartitions(), is(7));
    }
}
