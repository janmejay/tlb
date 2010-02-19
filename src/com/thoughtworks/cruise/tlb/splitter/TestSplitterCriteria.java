package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.util.List;
import java.io.File;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands the criteria for splitting a given test suite 
 */
public abstract class TestSplitterCriteria {
    protected File dir;
    protected final SystemEnvironment env;

    protected TestSplitterCriteria(SystemEnvironment env) {
        this.env = env;
    }

    public abstract List<FileResource> filter(List<FileResource> fileResources);

    public void setDir(File dir) {
        this.dir = dir;
    }
}
