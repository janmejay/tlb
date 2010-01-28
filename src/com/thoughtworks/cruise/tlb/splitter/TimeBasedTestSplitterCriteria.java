package com.thoughtworks.cruise.tlb.splitter;

import java.io.File;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria implements TestSplitterCriteria {
    public boolean shouldInclude(File file) {
        throw new RuntimeException("Not yet implemented");
    }
}
