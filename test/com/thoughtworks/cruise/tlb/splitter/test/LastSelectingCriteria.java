package com.thoughtworks.cruise.tlb.splitter.test;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import org.apache.tools.ant.types.resources.FileResource;
import org.jaxen.util.SingletonList;

import java.util.List;
import java.util.Arrays;

public class LastSelectingCriteria extends TestSplitterCriteria {
    public LastSelectingCriteria(SystemEnvironment env) {
        super(env);
    }

    public List<FileResource> filter(List<FileResource> fileResources) {
        return Arrays.asList(fileResources.get(fileResources.size() - 1));
    }
}