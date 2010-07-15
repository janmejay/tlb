package com.github.tlb.orderer;

import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.utils.SystemEnvironment;

import java.util.Comparator;

/**
 * @understands ordering of tests
 */
public abstract class TestOrderer implements Comparator<TlbSuiteFile> {
    protected final SystemEnvironment environment;

    protected TestOrderer(SystemEnvironment environment) {
        this.environment = environment;
    }

    public static final TestOrderer NO_OP = new TestOrderer(new SystemEnvironment()) {
        public int compare(TlbSuiteFile o1, TlbSuiteFile o2) {
            return 0;
        }
    };
}
