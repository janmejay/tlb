package com.thoughtworks.cruise.tlb.splitter.test;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.tools.ant.types.resources.FileResource;

import java.util.List;

public class UnusableCriteria2 extends TestSplitterCriteria {
    public UnusableCriteria2(SystemEnvironment env) {
        super(env);
    }

    public List<FileResource> filter(List<FileResource> fileResources) {
        throw new RuntimeException("Unusable criteira #2 won't work!");
    }
}