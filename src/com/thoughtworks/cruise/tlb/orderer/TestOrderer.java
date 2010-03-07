package com.thoughtworks.cruise.tlb.orderer;

import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

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
