package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteTimeEntry;

/**
 * @understands storage and retrival of time that each suite took to run
 */
public class SuiteTimeRepo extends SuiteEntryRepo<SuiteTimeEntry> {

    @Override
    protected SuiteTimeEntry getEntry(String record) {
        return SuiteTimeEntry.parseSingleEntry(record);
    }
}
