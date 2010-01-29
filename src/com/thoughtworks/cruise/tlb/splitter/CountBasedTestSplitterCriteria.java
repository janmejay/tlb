package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.types.resources.FileResource;

/**
 * @understands the criteria for splitting tests based on the number of tests
 */
public class CountBasedTestSplitterCriteria implements TestSplitterCriteria {
    private final TalkToCruise talkToCruise;
    private final SystemEnvironment env;

    public CountBasedTestSplitterCriteria(TalkToCruise talkToCruise, SystemEnvironment env) {
        this.talkToCruise = talkToCruise;
        this.env = env;
    }

    /**
     * This method needs to split based on the job that is being executed. That means the index of the job is to be used
     * like an iterator index, but in a distributed fashion. The solution is as follows:
     *
     * Eg: 37 tests split across 7 jobs. The output is 5 (2/7), 5 (4/7), 5 (6/7), 6 (8/7), 5 (3/7), 5 (5/7), 6 (7/7)
     * where each of (2/7) is basically the rate at which we carry over the balance before we account for it. 
     *
     * @param files
     * @return filtered list
     */
    public List<FileResource> filter(List<FileResource> files) {
        List<String> jobs = talkToCruise.getJobs(stageName());
        Collections.sort(jobs);

        int index = jobs.indexOf(jobName());
        int splitRatio = files.size() / jobs.size();
        int reminder = files.size() % jobs.size();

        double balance = (double) (reminder * (index + 1)) / jobs.size();
        int startIndex = isFirst(index) ? 0 : index * splitRatio + (int) Math.floor(balance - 1);
        int endIndex = isLast(jobs, index) ? files.size() : (index + 1) * splitRatio + (int) Math.floor(balance);

        return files.subList(startIndex, endIndex);
    }

    private boolean isLast(List<String> jobs, int index) {
        return (index + 1) == jobs.size();
    }

    private boolean isFirst(int index) {
        return (index == 0);
    }

    private String jobName() {
        return env.getProperty(TlbConstants.CRUISE_JOB_NAME);
    }

    private String stageName() {
        return env.getProperty(TlbConstants.CRUISE_STAGE_NAME);
    }
}
