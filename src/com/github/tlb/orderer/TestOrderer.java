package com.github.tlb.orderer;

import com.github.tlb.TlbFileResource;
import com.github.tlb.utils.SystemEnvironment;

import java.util.Comparator;

/**
 * @understands ordering of tests
 */
public abstract class TestOrderer implements Comparator<TlbFileResource> {
    protected final SystemEnvironment environment;

    protected TestOrderer(SystemEnvironment environment) {
        this.environment = environment;
    }

    public static final TestOrderer NO_OP = new TestOrderer(new SystemEnvironment()) {
        public int compare(TlbFileResource o1, TlbFileResource o2) {
            return 0;
        }
    };
}
