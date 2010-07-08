package com.github.tlb.splitter;

import com.github.tlb.TlbFileResource;
import com.github.tlb.service.TalkToService;
import com.github.tlb.utils.SystemEnvironment;

import java.util.List;
import java.util.logging.Logger;

/**
 * @understands the criteria for splitting tests based on the number of tests
 */
public class CountBasedTestSplitterCriteria extends JobFamilyAwareSplitterCriteria {
    private static final Logger logger = Logger.getLogger(CountBasedTestSplitterCriteria.class.getName());

    public CountBasedTestSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    CountBasedTestSplitterCriteria(TalkToService talkToService, SystemEnvironment env) {
        this(env);
        talksToService(talkToService);
    }

    /**
     * This method needs to split based on the job that is being executed. That means the index of the job is to be used
     * like an iterator index, but in a distributed fashion. The solution is as follows:
     * <p/>
     * Eg: 37 tests split across 7 jobs. The output is 5 (2/7), 5 (4/7), 5 (6/7), 6 (8/7), 5 (3/7), 5 (5/7), 6 (7/7)
     * where each of (2/7) is basically the rate at which we carry over the balance before we account for it.
     *
     * @param files
     * @return filtered load
     */
    protected List<TlbFileResource> subset(List<TlbFileResource> files) {
        int index = talkToService.partitionNumber() - 1;
        int splitRatio = files.size() / totalPartitions;
        int reminder = files.size() % totalPartitions;
        logger.info(String.format("count balancing to approximately %s files per job with %s extra file to bucket", splitRatio, reminder));

        double balance = (double) (reminder * (index + 1)) / totalPartitions;
        double lastBalance = (double) (reminder * index) / totalPartitions;
        int startIndex = isFirst(index) ? 0 : index * splitRatio + (int) Math.floor(Math.abs(lastBalance));
        int endIndex = isLast(totalPartitions, index) ? files.size() : (index + 1) * splitRatio + (int) Math.floor(balance);

        return files.subList(startIndex, endIndex);
    }

}
