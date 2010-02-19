package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.tools.ant.types.resources.FileResource;

import java.util.*;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria extends JobFamilyAwareSplitterCriteria implements TalksToCruise {

    public TimeBasedTestSplitterCriteria(SystemEnvironment environment) {
        super(environment);
    }

    public TimeBasedTestSplitterCriteria(TalkToCruise talkToCruise, SystemEnvironment env) {
        super(talkToCruise, env);
    }

    protected List<FileResource> subset(List<FileResource> fileResources) {
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

    private List<TestFile> testFiles(List<String> jobs, List<FileResource> fileResources) {
        Map<String, String> classToTime = talkToCruise.getLastRunTestTimes(jobs);
        Map<String, FileResource> fileNameToResource = new HashMap<String, FileResource>();
        Set<String> currentFileNames = new HashSet<String>();
        for (FileResource fileResource : fileResources) {
            String name = fileResource.getName();
            currentFileNames.add(name);
            fileNameToResource.put(name, fileResource);
        }

        List<TestFile> testFiles = new ArrayList<TestFile>();
        double totalTime = 0;

        for (String testClass : classToTime.keySet()) {
            String fileName = FileUtil.classFileRelativePath(testClass);
            double time = Double.parseDouble(classToTime.get(testClass));
            totalTime += time;
            if (currentFileNames.remove(fileName)) testFiles.add(new TestFile(fileNameToResource.get(fileName), time));
        }

        double avgTime = totalTime / classToTime.size();

        for (String newFile : currentFileNames) {
            testFiles.add(new TestFile(fileNameToResource.get(newFile), avgTime));
        }

        Collections.sort(testFiles);
        
        return testFiles;
    }

    private List<FileResource> resourcesFrom(Bucket bucket) {
        List<FileResource> resources = new ArrayList<FileResource>();
        for (FileResource file : bucket.files) {
            resources.add(file);
        }
        return resources;
    }

    private class Bucket implements Comparable<Bucket> {

        String name;
        Double time = 0.0;
        List<FileResource> files = new ArrayList<FileResource>();

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
        FileResource fileName;
        Double time;

        public TestFile(FileResource fileName, Double time) {
            this.fileName = fileName;
            this.time = time;
        }

        public int compareTo(TestFile o) {
            return o.time.compareTo(time);
        }
    }
}
