package com.thoughtworks.cruise.tlb.splitter.test;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.util.List;
import java.util.Arrays;

public class LastSelectingCriteria extends TestSplitterCriteria {
    public LastSelectingCriteria(SystemEnvironment env) {
        super(env);
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        return Arrays.asList(fileResources.get(fileResources.size() - 1));
    }
}