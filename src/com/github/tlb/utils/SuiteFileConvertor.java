package com.github.tlb.utils;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @understands converting testing/build framework specific implementation to tlb generic file entry
 */
public class SuiteFileConvertor {

    private Map<TlbSuiteFile, TlbFileResource> suiteFileToResource = new HashMap<TlbSuiteFile, TlbFileResource>();
    private List<TlbSuiteFile> suiteFiles = new ArrayList<TlbSuiteFile>();

    public synchronized List<TlbSuiteFile> toTlbSuiteFiles(List<TlbFileResource> fileResources) {
        if(! suiteFileToResource.isEmpty()) throw new IllegalStateException("overwriting of suite resource list is not allowed, new instance should be used");
        for (TlbFileResource fileResource : fileResources) {
            final TlbSuiteFileImpl suiteFile = new TlbSuiteFileImpl(fileResource.getName());
            suiteFileToResource.put(suiteFile, fileResource);
            suiteFiles.add(suiteFile);
        }
        return suiteFiles;
    }

    public List<TlbFileResource> toTlbFileResources(List<TlbSuiteFile> suiteFiles) {
        final List<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (TlbSuiteFile suiteFile : suiteFiles) {
            resources.add(suiteFileToResource.get(suiteFile));
        }
        return resources;
    }
}
