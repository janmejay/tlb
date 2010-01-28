package com.thoughtworks.cruise.tlb.splitter;

import java.io.File;

/**
 * @understands the criteria for splitting tests based on the number of tests
 */
public class CountBasedTestSplitterCriteria implements TestSplitterCriteria {

    public boolean shouldInclude(File file) {
        throw new RuntimeException("Not yet implemented");
    }
}
