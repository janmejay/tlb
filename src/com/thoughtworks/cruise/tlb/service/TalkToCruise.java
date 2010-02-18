package com.thoughtworks.cruise.tlb.service;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import static com.thoughtworks.cruise.tlb.TlbConstants.*;
import com.thoughtworks.cruise.tlb.service.http.HttpAction;
import com.thoughtworks.cruise.tlb.TlbConstants;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @understands requesting and posting information to/from cruise
 */
public class TalkToCruise {
    private static final Log LOG = LogFactory.getLog(TalkToCruise.class);

    private final SystemEnvironment environment;
    private final HttpAction httpAction;
    private static final String JOB_NAME = "name";
    protected static final String TEST_TIME_FILE = "tlb/test_time.properties";
    private static final Pattern STAGE_LOCATOR = Pattern.compile("(.*?)/\\d+/(.*?)/\\d+");
    private static final Pattern SUITE_TIME_STRING = Pattern.compile("(.*?):\\s*(\\d+)");
    private Integer subsetSize;
    final String jobLocator;
    final String stageLocator;
    final String subsetSizeUrl;

    public TalkToCruise(SystemEnvironment environment, HttpAction httpAction) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", "http://www.w3.org/2005/Atom");
        DocumentFactory factory = DocumentFactory.getInstance();
        factory.setXPathNamespaceURIs(map);
        this.environment = environment;
        this.httpAction = httpAction;
        subsetSize = null;
        jobLocator = String.format("%s/%s/%s/%s/%s", p(CRUISE_PIPELINE_NAME), p(CRUISE_PIPELINE_LABEL), p(CRUISE_STAGE_NAME), p(CRUISE_STAGE_COUNTER), p(CRUISE_JOB_NAME));
        stageLocator = String.format("%s/%s/%s/%s", p(CRUISE_PIPELINE_NAME), p(CRUISE_PIPELINE_COUNTER), p(CRUISE_STAGE_NAME), p(CRUISE_STAGE_COUNTER));
        subsetSizeUrl = String.format("%s/properties/%s/%s", cruiseUrl(), jobLocator, TlbConstants.TEST_SUBSET_SIZE);
    }

    public List<String> getJobs() {
        ArrayList<String> jobNames = new ArrayList<String>();
        for (Attribute jobLink : jobLinks(String.format("%s/pipelines/%s.xml", cruiseUrl(), stageLocator))) {
            jobNames.add(rootFor(jobLink.getValue()).attributeValue(JOB_NAME));
        }
        return jobNames;
    }

    @SuppressWarnings({"unchecked"})
    private List<Attribute> jobLinks(String url) {
        Element stage = rootFor(url);
        return (List<Attribute>) stage.selectNodes("//jobs/job/@href");
    }

    public Element rootFor(String url) {
        SAXReader builder = new SAXReader();
        String xmlString = httpAction.get(url);
        try {
            Document dom = builder.read(new StringReader(xmlString));
            return dom.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("XML could not be understood -> " + xmlString, e);
        }
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
        List<String> testTimes = cacheAndPersist(String.format("%s: %s\n", className, time), jobLocator);
        if (subsetSize() == testTimes.size()) {
            StringBuffer buffer = new StringBuffer();
            for (String testTime : testTimes) {
                buffer.append(testTime);
                buffer.append("\n");
            }
            httpAction.put(String.format("%s/files/%s/%s", cruiseUrl(), jobLocator, TEST_TIME_FILE), buffer.toString());
            clearSuiteTimeCachingFile();
        }

    }

    private List<String> cacheAndPersist(String line, String fileIdentifier) {
        File cacheFile = FileUtil.getUniqueFile(fileIdentifier);
        FileOutputStream out = null;
        FileInputStream in = null;
        List<String> lines = null;
        try {
            out = new FileOutputStream(cacheFile, true);
            IOUtils.write(line, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        try {
            in = new FileInputStream(cacheFile);
            lines = IOUtils.readLines(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return lines;
    }

    private int subsetSize() {
        if (subsetSize == null) {
            String propertyValue = httpAction.get(subsetSizeUrl).split("\n")[1];
            subsetSize = Integer.parseInt(propertyValue);
        }
        return subsetSize;
    }

    public Map<String, String> getLastRunTestTimes(List<String> jobNames) {
        String stageFeedUrl = String.format("%s/api/feeds/stages.xml", cruiseUrl());
        String stageDetailUrl = lastRunStageDetailUrl(stageFeedUrl);
        List<Attribute> jobLinks = jobLinks(stageDetailUrl);
        List<String> tlbTestTimeUrls = tlbTestTimeUrls(jobLinks, jobNames);
        return mergedProperties(tlbTestTimeUrls);
    }

    private Map<String, String> mergedProperties(List<String> tlbTestTimeUrls) {
        HashMap<String, String> suiteTimeMap = new HashMap<String, String>();
        StringBuffer buffer = new StringBuffer();
        for (String tlbTestTimeUrl : tlbTestTimeUrls) {
            try {
                buffer.append(httpAction.get(tlbTestTimeUrl) + "\n");
            } catch (RuntimeException e) {
                continue; //FIXME!
            }
        }
        StringTokenizer suiteTimes = new StringTokenizer(buffer.toString(), "\n");
        while (suiteTimes.hasMoreTokens()) {
            String tuple = suiteTimes.nextToken();
            Matcher matcher = SUITE_TIME_STRING.matcher(tuple);
            if (matcher.matches()) suiteTimeMap.put(matcher.group(1), matcher.group(2));
        }
        return suiteTimeMap;
    }

    private List<String> tlbTestTimeUrls(List<Attribute> jobLinks, List<String> jobNames) {
        ArrayList<String> tlbTestTimeUrls = new ArrayList<String>();
        for (Attribute jobLink : jobLinks) {
            Element jobDom = rootFor(jobLink.getValue());
            String jobName = jobDom.attribute("name").getValue().trim();
            if (jobNames.contains(jobName)) {
                String atrifactBaseUrl = jobDom.selectSingleNode("//artifacts/@baseUrl").getText();
                tlbTestTimeUrls.add(String.format("%s/%s", atrifactBaseUrl, TEST_TIME_FILE));
            }
        }
        return tlbTestTimeUrls;
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
        Map<String, String> payload = new HashMap<String, String>();
        payload.put("value", String.valueOf(size));
        httpAction.post(subsetSizeUrl, payload);
    }

    public void clearSuiteTimeCachingFile() {
        try {
            FileUtils.forceDelete(FileUtil.getUniqueFile(jobLocator));
        } catch (IOException e) {
            LOG.error("could not delete suite time cache file: " + e.getMessage());
        }
    }
}
