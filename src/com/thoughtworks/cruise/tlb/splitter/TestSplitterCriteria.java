package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @understands the criteria for splitting a given test suite
 */
public interface TestSplitterCriteria {
    TestSplitterCriteria MATCH_ALL_FILE_SET = new TestSplitterCriteria() {
        public List<FileResource> filter(List<FileResource> files) {
            return files;
        }
    };

    List<FileResource> filter(List<FileResource> files);
}
