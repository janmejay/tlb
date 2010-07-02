package com.github.tlb.server;

import com.github.tlb.domain.SuiteTimeEntry;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SuiteTimeRepoTest {
    private SuiteTimeRepo repo;

    @Before
    public void setUp() throws Exception {
        repo = new SuiteTimeRepo();
    }

    @Test
    public void shouldReturnSuiteTimeEntryForGivenRecord() {
        assertThat(repo.getEntry("foo.bar.Baz: 102"), is(new SuiteTimeEntry("foo.bar.Baz", 102l)));
        assertThat(repo.getEntry("bar.baz.Quux: 15"), is(new SuiteTimeEntry("bar.baz.Quux", 15l)));
    }
}
