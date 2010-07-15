package com.github.tlb.domain;

/**
 * @understands test suite that represents a balanceable and reorderable set of tests
 */
public class TlbTestSuite {
    private final String identifier;

    public TlbTestSuite(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TlbTestSuite testSuite = (TlbTestSuite) o;

        if (!identifier.equals(testSuite.identifier)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
