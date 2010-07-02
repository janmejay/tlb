package com.github.tlb.server;

import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.SuiteTimeEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @understands storage and retrival of time that each suite took to run
 */
public class SuiteTimeRepo extends SuiteEntryRepo<SuiteTimeEntry> {

    @Override
    protected SuiteTimeEntry getEntry(String record) {
        return SuiteTimeEntry.parseSingleEntry(record);
    }
}
