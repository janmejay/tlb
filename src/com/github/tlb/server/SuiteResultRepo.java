package com.github.tlb.server;

import com.github.tlb.domain.SuiteResultEntry;

/**
 * @understands storage and retrival of suite results for suites
 */
public class SuiteResultRepo extends SuiteEntryRepo<SuiteResultEntry> {
    @Override
    protected SuiteResultEntry getEntry(String record) {
        return SuiteResultEntry.parseSingleEntry(record);
    }
}
