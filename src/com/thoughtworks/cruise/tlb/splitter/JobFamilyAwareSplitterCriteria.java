package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
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

    private static final Logger logger = Logger.getLogger(JobFamilyAwareSplitterCriteria.class.getName());

    public JobFamilyAwareSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        logger.info(String.format("got total of %s files to balance", fileResources.size()));

        jobs = pearJobs();
        logger.info(String.format("total jobs to distribute load [ %s ]", jobs.size()));
        if (jobs.size() <= 1) {
            return fileResources;
        }

        List<TlbFileResource> subset = subset(fileResources);
        logger.info(String.format("assigned total of %s files to [ %s ]", subset.size(), env.getProperty(TlbConstants.CRUISE_JOB_NAME)));
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
