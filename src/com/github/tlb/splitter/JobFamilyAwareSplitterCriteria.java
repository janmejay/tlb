package com.github.tlb.splitter;

import com.github.tlb.TlbConstants;
import com.github.tlb.TlbFileResource;

import java.util.List;
import java.util.logging.Logger;

import com.github.tlb.TlbSuiteFile;
import com.github.tlb.service.TalkToService;
import com.github.tlb.utils.SuiteFileConvertor;
import com.github.tlb.utils.SystemEnvironment;

/**
 * @understands the criteria for splitting a given test suite across jobs from the same family
 */
public abstract class JobFamilyAwareSplitterCriteria extends TestSplitterCriteria implements TalksToService {
    public static TestSplitterCriteria MATCH_ALL_FILE_SET = new JobFamilyAwareSplitterCriteria(null) {
        @Override
        public List<TlbSuiteFile> filterSuites(List<TlbSuiteFile> fileResources) {
            return fileResources;
        }

        protected List<TlbSuiteFile> subset(List<TlbSuiteFile> fileResources) {
            throw new RuntimeException("Should never reach here");
        }
    };
    protected TalkToService talkToService;

    private static final Logger logger = Logger.getLogger(JobFamilyAwareSplitterCriteria.class.getName());
    protected int totalPartitions;

    public JobFamilyAwareSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    @Override
    public List<TlbSuiteFile> filterSuites(List<TlbSuiteFile> fileResources) {
        logger.info(String.format("got total of %s files to balance", fileResources.size()));

        totalPartitions = talkToService.totalPartitions();
        logger.info(String.format("total jobs to distribute load [ %s ]", totalPartitions));
        if (totalPartitions <= 1) {
            return fileResources;
        }

        List<TlbSuiteFile> subset = subset(fileResources);
        logger.info(String.format("assigned total of %s files to [ %s ]", subset.size(), env.getProperty(TlbConstants.Cruise.CRUISE_JOB_NAME)));
        talkToService.publishSubsetSize(subset.size());
        return subset;
    }

    protected abstract List<TlbSuiteFile> subset(List<TlbSuiteFile> fileResources);

    public void talksToService(TalkToService service) {
       this.talkToService = service;
    }

    protected boolean isLast(int totalPartitions, int index) {
        return (index + 1) == totalPartitions;
    }

    protected boolean isFirst(int index) {
        return (index == 0);
    }

    protected String jobName() {
        return env.getProperty(TlbConstants.Cruise.CRUISE_JOB_NAME);
    }

}
