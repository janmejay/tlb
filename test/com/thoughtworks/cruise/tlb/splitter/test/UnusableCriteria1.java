package com.thoughtworks.cruise.tlb.splitter.test;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbFileResource;

import java.util.List;

public class UnusableCriteria1 extends TestSplitterCriteria {
    public UnusableCriteria1(SystemEnvironment env) {
        super(env);
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        throw new RuntimeException("Unusable criteira #1 won't work!");
    }
}
