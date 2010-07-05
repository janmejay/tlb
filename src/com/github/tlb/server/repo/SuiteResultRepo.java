package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.SuiteResultEntry;

import java.io.IOException;
import java.util.Collection;

/**
 * @understands storage and retrival of suite results for suites
 */
public class SuiteResultRepo extends SuiteEntryRepo<SuiteResultEntry> {

    public Collection<SuiteResultEntry> list(String version) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("versioning not allowed");
    }
}
