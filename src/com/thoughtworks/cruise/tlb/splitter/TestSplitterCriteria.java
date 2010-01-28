package com.thoughtworks.cruise.tlb.splitter;

import java.io.File;

/**
 * @understands the criteria for splitting a given test suite
 */
public interface TestSplitterCriteria {
    TestSplitterCriteria MATCH_ALL_FILE_SET = new TestSplitterCriteria() {
        public boolean shouldInclude(File file) {
            return true;
        }
    };

    boolean shouldInclude(File file);
}
