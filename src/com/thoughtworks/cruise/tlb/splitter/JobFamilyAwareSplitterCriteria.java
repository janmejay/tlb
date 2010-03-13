package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands the criteria for splitting a given test suite across jobs from the same family
 */
public abstract class JobFamilyAwareSplitterCriteria extends TestSplitterCriteria implements TalksToCruise {
    public static TestSplitterCriteria MATCH_ALL_FILE_SET = new JobFamilyAwareSplitterCriteria(null) {
        public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
            return fileResources;
        }

        protected List<TlbFileResource> subset(List<TlbFileResource> fileResources) {
            throw new RuntimeException("Should never reach here");
        }
    };
    protected TalkToCruise talkToCruise;
    protected List<String> jobs;

    public JobFamilyAwareSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        jobs = pearJobs();
        if (jobs.size() <= 1) {
            return fileResources;
        }

        List<TlbFileResource> subset = subset(fileResources);
        talkToCruise.publishSubsetSize(subset.size());
        return subset;
    }

    protected abstract List<TlbFileResource> subset(List<TlbFileResource> fileResources);

    public void talksToCruise(TalkToCruise cruise) {
       this.talkToCruise = cruise;
    }

    protected boolean isLast(List<String> jobs, int index) {
        return (index + 1) == jobs.size();
    }

    protected boolean isFirst(int index) {
        return (index == 0);
    }

    protected String jobName() {
        return env.getProperty(TlbConstants.CRUISE_JOB_NAME);
    }

    protected List<String> pearJobs() {
        return talkToCruise.pearJobs();
    }
}
