package com.github.tlb.service.http;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * ensures suite count is above 1, so balancing happens
 */
public class ToKeepCountOverThresholdTest {
    @Test
    public void shouldNoOp() throws Exception{
        assertThat(0, is(0));
    }
}
