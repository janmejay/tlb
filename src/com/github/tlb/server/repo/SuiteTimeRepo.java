package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteTimeEntry;

import java.io.IOException;

/**
 * @understands storage and retrival of time that each suite took to run
 */
public class SuiteTimeRepo extends VersioningEntryRepo<SuiteTimeEntry> {

    @Override
    public SuiteTimeRepo getSubRepo(String versionIdentifier) throws IOException, ClassNotFoundException {
        return factory.createSuiteTimeRepo(namespace, versionIdentifier);
    }
}
