package com.github.tlb.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TlbTestSuiteTest {

    @Test
    public void shouldKnowAnAbstractIdentifierThatUniquelyIdentifiesATest() {
        final TlbTestSuite testSuite = new TlbTestSuite("foo/Bar.class");
        assertThat(testSuite, is(new TlbTestSuite("foo/Bar.class")));
    }
    
    @Test
    public void shouldStringRepresentItselfAsIdentifier() {
        final TlbTestSuite testSuite = new TlbTestSuite("foo/Bar.class");
        assertThat(testSuite.toString(), is("foo/Bar.class"));
    }
}
