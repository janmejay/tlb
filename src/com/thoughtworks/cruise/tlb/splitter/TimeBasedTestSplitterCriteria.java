package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.TlbConstants;

import java.util.*;
import java.util.logging.Logger;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria extends JobFamilyAwareSplitterCriteria implements TalksToCruise {
    private final FileUtil fileUtil;
    private static final Logger logger = Logger.getLogger(TimeBasedTestSplitterCriteria.class.getName());
    private static final String NO_HISTORICAL_DATA = "no historical test time data, aborting attempt to balance based on time";

    public TimeBasedTestSplitterCriteria(TalkToCruise talkToCruise, SystemEnvironment env) {
        this(env);
        talksToCruise(talkToCruise);
    }

    public TimeBasedTestSplitterCriteria(SystemEnvironment env) {
        super(env);
        fileUtil = new FileUtil(env);
    }

    protected List<TlbFileResource> subset(List<TlbFileResource> fileResources) {
        List<TestFile> testFiles = testFiles(jobs, fileResources);
        Bucket thisBucket = buckets(jobs, testFiles);

        return resourcesFrom(thisBucket);
    }

    private Bucket buckets(List<String> jobs, List<TestFile> testFiles) {
        Bucket thisBucket = null;
        List<Bucket> buckets = new ArrayList<Bucket>();

        for (String job : jobs) {
            Bucket bucket = new Bucket(job);
            if (job.equals(jobName())) thisBucket = bucket;
            buckets.add(bucket);
        }

        assignToBuckets(testFiles, buckets);

        return thisBucket;
    }

    private void assignToBuckets(List<TestFile> testFiles, List<Bucket> buckets) {
        for (TestFile testFile : testFiles) {
            buckets.get(0).add(testFile);
            Collections.sort(buckets);
        }
    }

    private List<TestFile> testFiles(List<String> jobs, List<TlbFileResource> fileResources) {
        Map<String, String> classToTime = talkToCruise.getLastRunTestTimes(jobs);
        logger.info(String.format("historical test time data has entries for %s suites", classToTime.size()));
        if (classToTime.isEmpty()) {
            logger.warning(NO_HISTORICAL_DATA);
            throw new IllegalStateException(NO_HISTORICAL_DATA);
        }
        Map<String, TlbFileResource> fileNameToResource = new HashMap<String, TlbFileResource>();
        Set<String> currentFileNames = new HashSet<String>();
        for (TlbFileResource fileResource : fileResources) {
            String name = fileResource.getName();
            currentFileNames.add(name);
            fileNameToResource.put(name, fileResource);
        }

        List<TestFile> testFiles = new ArrayList<TestFile>();
        double totalTime = 0;

        for (String testClass : classToTime.keySet()) {
            String fileName = fileUtil.classFileRelativePath(testClass);
            double time = Double.parseDouble(classToTime.get(testClass));
            totalTime += time;
            if (currentFileNames.remove(fileName)) testFiles.add(new TestFile(fileNameToResource.get(fileName), time));
        }
        logger.info(String.format("%s entries of historical test time data found relavent", testFiles.size()));

        double avgTime = totalTime / classToTime.size();

        logger.info(String.format("encountered %s new files which don't have historical time data, used average time [ %s ] to balance", currentFileNames.size(), avgTime));

        for (String newFile : currentFileNames) {
            testFiles.add(new TestFile(fileNameToResource.get(newFile), avgTime));
        }

        Collections.sort(testFiles);
        
        return testFiles;
    }

    private List<TlbFileResource> resourcesFrom(Bucket bucket) {
        List<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (TlbFileResource file : bucket.files) {
            resources.add(file);
        }
        return resources;
    }

    private class Bucket implements Comparable<Bucket> {

        String name;
        Double time = 0.0;
        List<TlbFileResource> files = new ArrayList<TlbFileResource>();

        public Bucket(String name) {
            this.name = name;
        }

        public int compareTo(Bucket o) {
            return time.compareTo(o.time);
        }

        public void add(TestFile testFile) {
            files.add(testFile.fileName);
            time += testFile.time;
        }
    }

    private class TestFile implements Comparable<TestFile> {
        TlbFileResource fileName;
        Double time;

        public TestFile(TlbFileResource fileName, Double time) {
            this.fileName = fileName;
            this.time = time;
        }

        public int compareTo(TestFile o) {
            return o.time.compareTo(time);
        }
    }
}
