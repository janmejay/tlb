package com.github.tlb.splitter.test;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.splitter.TestSplitterCriteria;
import com.github.tlb.utils.SystemEnvironment;

import java.util.List;

public class UnusableCriteria2 extends TestSplitterCriteria {
    public UnusableCriteria2(SystemEnvironment env) {
        super(env);
    }

    @Override
    public List<TlbSuiteFile> filterSuites(List<TlbSuiteFile> fileResources) {
        throw new RuntimeException("Unusable criteira #2 won't work!");
    }
}