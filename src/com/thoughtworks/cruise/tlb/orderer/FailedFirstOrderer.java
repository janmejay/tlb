package com.thoughtworks.cruise.tlb.orderer;

import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @understands ordering to bring failed tests first
 */
public class FailedFirstOrderer extends TestOrderer {
    public FailedFirstOrderer(SystemEnvironment environment) {
        super(environment);
    }

    public int compare(TlbFileResource o1, TlbFileResource o2) {
        throw new NotImplementedException();
    }
}
