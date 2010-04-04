package com.github.tlb.service;

import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.domain.SuiteTimeEntry;

import java.util.List;

/**
 * @understands talking to an external service to get and post data
 */
public interface TalkToService {
    void testClassTime(String className, long time);

    void testClassFailure(String className, boolean hasFailed);

    List<SuiteTimeEntry> getLastRunTestTimes(List<String> jobNames);

    void publishSubsetSize(int size);

    void clearSuiteTimeCachingFile();

    List<SuiteResultEntry> getLastRunFailedTests(List<String> jobNames);

    List<String> pearJobs();
}
