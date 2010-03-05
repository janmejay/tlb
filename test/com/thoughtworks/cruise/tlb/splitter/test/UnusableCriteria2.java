package com.thoughtworks.cruise.tlb.splitter.test;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.util.List;

public class UnusableCriteria2 extends TestSplitterCriteria {
    public UnusableCriteria2(SystemEnvironment env) {
        super(env);
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        throw new RuntimeException("Unusable criteira #2 won't work!");
    }
}