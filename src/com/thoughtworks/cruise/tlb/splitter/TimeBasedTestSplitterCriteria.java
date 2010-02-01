package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.List;
import java.util.Iterator;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands criteria for splitting tests based on time taken
 */
public class TimeBasedTestSplitterCriteria extends TestSplitterCriteria {

    public TimeBasedTestSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    public List<FileResource> filter(List<FileResource> files) {
        throw new RuntimeException("Not yet implemented");
    }
}
