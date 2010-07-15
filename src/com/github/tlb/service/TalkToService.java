package com.github.tlb.service;

import com.github.tlb.domain.Entry;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;

import java.util.List;

/**
 * @understands talking to an external service to get and post data
 */
public interface TalkToService {
    //TODO: this is horrible api, make it accept SuiteTimeEntry
    void testClassTime(String className, long time);

    //TODO: this is horrible api, make it accept SuiteResultEntry
    void testClassFailure(String className, boolean hasFailed);

    List<SuiteTimeEntry> getLastRunTestTimes();

    List<SuiteResultEntry> getLastRunFailedTests();

    void publishSubsetSize(int size);

    void clearSuiteTimeCachingFile();

    int partitionNumber();

    int totalPartitions();
}
