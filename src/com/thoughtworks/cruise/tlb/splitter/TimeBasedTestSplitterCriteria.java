package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.List;
import java.util.Iterator;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria implements TestSplitterCriteria {

    public List<FileResource> filter(List<FileResource> files) {
        throw new RuntimeException("Not yet implemented");
    }
}
