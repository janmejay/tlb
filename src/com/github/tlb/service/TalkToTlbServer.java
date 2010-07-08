package com.github.tlb.service;

import com.github.tlb.TlbConstants;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.service.http.DefaultHttpAction;
import com.github.tlb.service.http.HttpAction;
import com.github.tlb.utils.SystemEnvironment;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.util.List;

import static com.github.tlb.TlbConstants.TlbServer.JOB_NAMESPACE;
import static com.github.tlb.TlbConstants.TlbServer.URL;

/**
 * @understands exchanging balancing/ordering related data with the TLB server
 */
public class TalkToTlbServer implements TalkToService {
    private final SystemEnvironment env;
    private final HttpAction httpAction;

    //reflectively invoked by factory
    public TalkToTlbServer(SystemEnvironment systemEnvironment) {
        this(systemEnvironment, createHttpAction(systemEnvironment));
    }

    public TalkToTlbServer(SystemEnvironment systemEnvironment, HttpAction httpAction) {
        env = systemEnvironment;
        this.httpAction = httpAction;
    }

    private static HttpAction createHttpAction(SystemEnvironment env) {
        final HttpClient client = new HttpClient(new HttpClientParams());
        final URI uri;
        try {
            uri = new URI(env.getProperty(TlbConstants.TlbServer.URL), true);
        } catch (URIException e) {
            throw new RuntimeException(e);
        }
        return new DefaultHttpAction(client, uri);
    }

    public void testClassTime(String className, long time) {
        httpAction.put(getUrl(namespace(), suiteTimeRepoName()), String.format("%s: %s", className, time));
    }

    private String suiteTimeRepoName() {
        return Boolean.parseBoolean(env.getProperty(TlbConstants.TlbServer.USE_SMOOTHING, "false")) ? EntryRepoFactory.SMOOTHED_SUITE_TIME : EntryRepoFactory.SUITE_TIME;
    }

    public void testClassFailure(String className, boolean hasFailed) {
        httpAction.put(suiteResultUrl(), new SuiteResultEntry(className, hasFailed).toString());
    }

    public List<SuiteTimeEntry> getLastRunTestTimes() {
        return SuiteTimeEntry.parse(httpAction.get(getUrl(namespace(), suiteTimeRepoName(), env.getProperty(TlbConstants.TlbServer.JOB_VERSION))));
    }

    public List<SuiteResultEntry> getLastRunFailedTests() {
        return SuiteResultEntry.parse(httpAction.get(suiteResultUrl()));
    }

    public void publishSubsetSize(int size) {
        httpAction.post(getUrl(jobName(), EntryRepoFactory.SUBSET_SIZE), String.valueOf(size));
    }

    public void clearSuiteTimeCachingFile() {
        //NOOP
        //TODO: if chattiness becomes a problem, this will need to be implemented sensibly
    }

    public int partitionNumber() {
        return Integer.parseInt(env.getProperty(TlbConstants.TlbServer.PARTITION_NUMBER));
    }

    public int totalPartitions() {
        return Integer.parseInt(env.getProperty(TlbConstants.TlbServer.TOTAL_PARTITIONS));
    }

    private String getUrl(String... parts) {
        final StringBuilder builder = new StringBuilder();
        builder.append(env.getProperty(URL));
        for (String part : parts) {
            builder.append("/").append(part);
        }
        return builder.toString();
    }

    private String suiteResultUrl() {
        return getUrl(namespace(), EntryRepoFactory.SUITE_RESULT);
    }

    private String jobName() {
        return String.format("%s-%s", namespace(), partitionNumber());
    }

    private String namespace() {
        return env.getProperty(JOB_NAMESPACE);
    }
}
