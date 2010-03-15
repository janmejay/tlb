package com.thoughtworks.cruise.tlb.service;

import com.thoughtworks.cruise.tlb.TlbConstants;
import static com.thoughtworks.cruise.tlb.TlbConstants.*;
import com.thoughtworks.cruise.tlb.service.http.HttpAction;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.utils.XmlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @understands requesting and posting information to/from cruise
 */
public class TalkToCruise {
    private static final Logger logger = Logger.getLogger(TalkToCruise.class.getName());

    private final SystemEnvironment environment;
    private final HttpAction httpAction;
    private static final String JOB_NAME = "name";
    protected static final String TEST_TIME_FILE = "tlb/test_time.properties";
    private static final Pattern STAGE_LOCATOR = Pattern.compile("(.*?)/\\d+/(.*?)/\\d+");
    private static final Pattern SUITE_TIME_STRING = Pattern.compile("(.*?):\\s*(\\d+)");
    private Integer subsetSize;
    final String jobLocator;
    final String stageLocator;
    final String testSubsetSizeFileLocator;
    private final FileUtil fileUtil;
    public static final String FAILED_TESTS_FILE = "tlb/failed_tests";
    public final String failedTestsListFileLocator;

    private static final String INT = "\\d+";
    private static final Pattern NUMBER_BASED_LOAD_BALANCED_JOB = Pattern.compile("(.*?)-(" + INT + ")");
    private static final String HEX = "[a-fA-F0-9]";
    private static final String UUID = HEX + "{8}-" + HEX + "{4}-" + HEX + "{4}-" + HEX + "{4}-" + HEX + "{12}";
    private static final Pattern UUID_BASED_LOAD_BALANCED_JOB = Pattern.compile("(.*?)-(" + UUID + ")");

    public TalkToCruise(SystemEnvironment environment, HttpAction httpAction) {
        this.environment = environment;
        this.httpAction = httpAction;
        subsetSize = null;
        jobLocator = String.format("%s/%s/%s/%s/%s", p(CRUISE_PIPELINE_NAME), p(CRUISE_PIPELINE_LABEL), p(CRUISE_STAGE_NAME), p(CRUISE_STAGE_COUNTER), p(CRUISE_JOB_NAME));
        testSubsetSizeFileLocator = String.format("%s/subset_size", jobLocator);
        failedTestsListFileLocator = String.format("%s/failed_tests", jobLocator);
        stageLocator = String.format("%s/%s/%s/%s", p(CRUISE_PIPELINE_NAME), p(CRUISE_PIPELINE_COUNTER), p(CRUISE_STAGE_NAME), p(CRUISE_STAGE_COUNTER));
        fileUtil = new FileUtil(environment);
    }

    public List<String> getJobs() {
        ArrayList<String> jobNames = new ArrayList<String>();
        for (Attribute jobLink : jobLinks(String.format("%s/pipelines/%s.xml", cruiseUrl(), stageLocator))) {
            jobNames.add(rootFor(jobLink.getValue()).attributeValue(JOB_NAME));
        }
        logger.info(String.format("jobs found %s", jobNames));
        return jobNames;
    }

    @SuppressWarnings({"unchecked"})
    private List<Attribute> jobLinks(String url) {
        Element stage = rootFor(url);
        return (List<Attribute>) stage.selectNodes("//jobs/job/@href");
    }

    public Element rootFor(String url) {
        String xmlString = httpAction.get(url);
        return XmlUtil.domFor(xmlString);
    }

    private Object cruiseUrl() {
        String url = p(CRUISE_SERVER_URL);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String p(String key) {
        return environment.getProperty(key);
    }

    public void testClassTime(String className, long time) {
        logger.info(String.format("recording run time for suite %s", className));
        List<String> testTimes = cacheAndPersist(String.format("%s: %s\n", className, time), jobLocator);
        if (subsetSize() == testTimes.size()) {
            logger.info(String.format("Posting test run times for %s suite to the cruise server.", subsetSize()));
            postLinesToServer(testTimes, artifactFileUrl(TEST_TIME_FILE));
            clearSuiteTimeCachingFile();
        }
    }

    private void postLinesToServer(List<String> testTimes, String url) {
        StringBuffer buffer = new StringBuffer();
        for (String testTime : testTimes) {
            buffer.append(testTime);
            buffer.append("\n");
        }
        httpAction.put(url, buffer.toString());
    }

    public void testClassFailure(String className, boolean hasFailed) {
        persist(String.format("%s: %s\n", className, hasFailed), failedTestsListFileLocator);
        List<String> runTests = cache(failedTestsListFileLocator);


        if (subsetSize() == runTests.size()) {
            List<String> failedTests = new ArrayList<String>();
            for (String runTest : runTests) {
                if (runTest.matches("(.+?)\\:\\strue$")) {
                    failedTests.add(runTest.substring(0, runTest.indexOf(":")));
                }
            }
            postLinesToServer(failedTests, artifactFileUrl(FAILED_TESTS_FILE));
        }
    }

    private String artifactFileUrl(String atrifactFile) {
        return String.format("%s/files/%s/%s", cruiseUrl(), jobLocator, atrifactFile);
    }

    private List<String> cacheAndPersist(String line, String fileIdentifier) {
        persist(line, fileIdentifier);
        return cache(fileIdentifier);
    }

    List<String> cache(String fileIdentifier) {
        File cacheFile = fileUtil.getUniqueFile(fileIdentifier);
        FileInputStream in = null;
        List<String> lines = null;
        if (!cacheFile.exists()) {
            return new ArrayList<String>();
        }
        try {
            in = new FileInputStream(cacheFile);
            lines = IOUtils.readLines(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        logger.info(String.format("Cached 3 lines from %s [ identified by: %s ], the last of which was [ %s ]", cacheFile.getAbsolutePath(), fileIdentifier, lines.get(lines.size() - 1)));
        return lines;
    }

    void persist(String line, String fileIdentifier) {
        File cacheFile = fileUtil.getUniqueFile(fileIdentifier);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(cacheFile, true);
            IOUtils.write(line, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        logger.info(String.format("Wrote [ %s ] to %s [ identified by: %s ]", line, fileUtil.getUniqueFile("foo"), fileIdentifier));
    }

    private int subsetSize() {
        if (subsetSize == null) {
            List<String> subsetSizes = cache(testSubsetSizeFileLocator);
            String propertyValue = subsetSizes.get(subsetSizes.size() - 1);
            subsetSize = Integer.parseInt(propertyValue);
        }
        return subsetSize;
    }

    public Map<String, String> getLastRunTestTimes(List<String> jobNames) {
        return mergedProperties(tlbArtifactPayloadLines(lastRunArtifactUrls(jobNames, TEST_TIME_FILE)));
    }

    private List<String> lastRunArtifactUrls(List<String> jobNames, String urlSuffix) {
        String stageFeedUrl = String.format("%s/api/feeds/stages.xml", cruiseUrl());
        String stageDetailUrl = lastRunStageDetailUrl(stageFeedUrl);
        List<Attribute> jobLinks = jobLinks(stageDetailUrl);
        return tlbArtifactUrls(jobLinks, jobNames, urlSuffix);
    }

    private Map<String, String> mergedProperties(StringTokenizer suiteTimeLines) {
        HashMap<String, String> suiteTimeMap = new HashMap<String, String>();
        while (suiteTimeLines.hasMoreTokens()) {
            String tuple = suiteTimeLines.nextToken();
            Matcher matcher = SUITE_TIME_STRING.matcher(tuple);
            if (matcher.matches()) suiteTimeMap.put(matcher.group(1), matcher.group(2));
        }
        return suiteTimeMap;
    }

    private StringTokenizer tlbArtifactPayloadLines(List<String> tlbTestTimeUrls) {
        StringBuffer buffer = new StringBuffer();
        for (String tlbTestTimeUrl : tlbTestTimeUrls) {
            try {
                buffer.append(httpAction.get(tlbTestTimeUrl) + "\n");
            } catch (RuntimeException e) {
                continue; //FIXME!
            }
        }
        return new StringTokenizer(buffer.toString(), "\n");
    }

    private List<String> tlbArtifactUrls(List<Attribute> jobLinks, List<String> jobNames, String urlSuffix) {
        ArrayList<String> tlbAtrifactUrls = new ArrayList<String>();
        for (Attribute jobLink : jobLinks) {
            Element jobDom = rootFor(jobLink.getValue());
            String jobName = jobDom.attribute("name").getValue().trim();
            if (jobNames.contains(jobName)) {
                String atrifactBaseUrl = jobDom.selectSingleNode("//artifacts/@baseUrl").getText();
                tlbAtrifactUrls.add(String.format("%s/%s", atrifactBaseUrl, urlSuffix));
            }
        }
        return tlbAtrifactUrls;
    }

    @SuppressWarnings({"unchecked"})
    private String lastRunStageDetailUrl(String stageFeedUrl) {
        Element stageFeedPage = rootFor(stageFeedUrl);
        List<Element> list = stageFeedPage.selectNodes("//a:entry");
        for (Element element : list) {
            String stageLocator = element.selectSingleNode("./a:title").getText();
            if (sameStage(stageLocator)) {
                return element.selectSingleNode("./a:link/@href").getText();
            }
        }
        return lastRunStageDetailUrl(stageFeedPage.selectSingleNode("//a:link[@rel='next']/@href").getText());
    }

    private boolean sameStage(String stageLocator) {
        Matcher matcher = STAGE_LOCATOR.matcher(stageLocator);
        if (!matcher.matches()) {
            return false;
        }
        boolean samePipeline = environment.getProperty(CRUISE_PIPELINE_NAME).equals(matcher.group(1));
        boolean sameStage = environment.getProperty(CRUISE_STAGE_NAME).equals(matcher.group(2));
        return samePipeline && sameStage;
    }

    public void publishSubsetSize(int size) {
        String line = String.valueOf(size) + "\n";
        persist(line, testSubsetSizeFileLocator);
        logger.info(String.format("Posting balanced subset size as %s to cruise server", size));
        httpAction.put(artifactFileUrl(TlbConstants.TEST_SUBSET_SIZE_FILE), line);
    }

    public void clearSuiteTimeCachingFile() {
        for (String fileIdentifier : Arrays.asList(jobLocator, testSubsetSizeFileLocator, failedTestsListFileLocator)) {
            try {
                FileUtils.forceDelete(fileUtil.getUniqueFile(fileIdentifier));
            } catch (IOException e) {
                logger.log(Level.WARNING, "could not delete suite time cache file: " + e.getMessage(), e);
            }
        }
    }

    public List<String> getLastRunFailedTests(List<String> jobNames) {
        StringTokenizer failedTestTokenizer = tlbArtifactPayloadLines(lastRunArtifactUrls(jobNames, FAILED_TESTS_FILE));
        ArrayList<String> failedTestNames = new ArrayList<String>();
        while(failedTestTokenizer.hasMoreTokens()) {
            failedTestNames.add(failedTestTokenizer.nextToken());
        }
        return failedTestNames;
    }

    protected String jobName() {
        return environment.getProperty(TlbConstants.CRUISE_JOB_NAME);
    }

    private String jobBaseName() {
        Matcher matcher = NUMBER_BASED_LOAD_BALANCED_JOB.matcher(jobName());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = UUID_BASED_LOAD_BALANCED_JOB.matcher(jobName());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return jobName();
    }

    private Pattern getMatcher() {
        return Pattern.compile(String.format("^%s-(" + INT + "|" + UUID + ")$", jobBaseName()));
    }

    private List<String> jobsInTheSameFamily(List<String> jobs) {
        List<String> family = new ArrayList<String>();
        Pattern pattern = getMatcher();
        for (String job : jobs) {
            if (pattern.matcher(job).matches()) {
                family.add(job);
            }
        }
        return family;
    }

    public List<String> pearJobs() {
        List<String> jobs = jobsInTheSameFamily(getJobs());
        Collections.sort(jobs);
        return jobs;
    }
}
