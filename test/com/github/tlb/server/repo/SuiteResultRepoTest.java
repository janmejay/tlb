package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteResultEntry;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SuiteResultRepoTest {
    private SuiteResultRepo repo;

    @Before
    public void setUp() throws Exception {
        repo = new SuiteResultRepo();
    }

    @Test
    public void shouldNotAllowVersioning() {
        try {
            repo.list("foo");
            fail("should not have allowed versioning");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("versioning not allowed"));
        }
    }
}
