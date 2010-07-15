package com.github.tlb.splitter.test;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.splitter.TestSplitterCriteria;
import com.github.tlb.utils.SystemEnvironment;

import java.util.List;
import java.util.Arrays;

public class LastSelectingCriteria extends TestSplitterCriteria {
    public LastSelectingCriteria(SystemEnvironment env) {
        super(env);
    }

    @Override
    public List<TlbSuiteFile> filterSuites(List<TlbSuiteFile> fileResources) {
        return Arrays.asList(fileResources.get(fileResources.size() - 1));
    }
}