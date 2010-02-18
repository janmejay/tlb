package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.util.List;
import java.io.File;

/**
 * @understands the criteria for splitting a given test suite 
 */
public abstract class TestSplitterCriteria {
    protected File dir;

    public abstract List<FileResource> filter(List<FileResource> fileResources);

    public void setDir(File dir) {
        this.dir = dir;
    }
}
