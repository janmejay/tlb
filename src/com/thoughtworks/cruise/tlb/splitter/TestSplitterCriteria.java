package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.TlbFileResource;

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

    public abstract List<TlbFileResource> filter(List<TlbFileResource> fileResources);

    public void setDir(File dir) {
        this.dir = dir;
    }
}
