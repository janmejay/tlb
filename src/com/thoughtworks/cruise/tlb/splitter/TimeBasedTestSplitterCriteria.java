package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.Project;

import java.io.File;
import java.util.*;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.TlbConstants;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria extends TestSplitterCriteria implements TalksToCruise {

    public TimeBasedTestSplitterCriteria(SystemEnvironment environment) {
        super(environment);
    }

    public TimeBasedTestSplitterCriteria(TalkToCruise talkToCruise, SystemEnvironment env) {
        super(talkToCruise, env);
    }

    public List<FileResource> filter(List<FileResource> fileResources) {
        List<String> jobs = jobsInTheSameFamily(talkToCruise.getJobs());
        if (jobs.size() <= 1) {
            return fileResources;
        }
        Collections.sort(jobs);
        Map<String, String> classToTime = talkToCruise.getTestTimes(jobs);

        Set<TestFile> testFiles = new TreeSet<TestFile>();

        for (String testClass : classToTime.keySet()) {
            String fileName = FileUtil.getCannonicalName(testClass);
            testFiles.add(new TestFile(fileName, Double.parseDouble(classToTime.get(testClass))));
        }

        List<Bucket> buckets = new ArrayList<Bucket>();

        for (String job : jobs) {
            buckets.add(new Bucket(job));
        }

        for (TestFile testFile : testFiles) {
            buckets.get(0).add(testFile);
            Collections.sort(buckets);
        }

        for (Bucket bucket : buckets) {
            if (bucket.name.equals(jobName())) {
                return resourcesFrom(bucket, fileResources.get(0).getProject());
            }
        }
        throw new RuntimeException("Should never get here! Cannot find the job with name: " + jobName());
    }

    private List<FileResource> resourcesFrom(Bucket bucket, Project project) {
        List<FileResource> resources = new ArrayList<FileResource>();
        for (String file : bucket.files) {
            resources.add(new FileResource(project, file));
        }
        return resources;
    }

    private class Bucket implements Comparable<Bucket> {

        String name;
        Double time = 0.0;
        List<String> files = new ArrayList<String>();

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
        String fileName;
        Double time;

        public TestFile(String fileName, Double time) {
            this.fileName = fileName;
            this.time = time;
        }

        public int compareTo(TestFile o) {
            return o.time.compareTo(time);
        }
    }
}
