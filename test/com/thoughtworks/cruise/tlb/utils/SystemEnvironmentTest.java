package com.thoughtworks.cruise.tlb.utils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.util.HashMap;

public class SystemEnvironmentTest {
    
    @Test
    public void shouldGetPropertyAvailableInGivenMap() throws Exception {
        SystemEnvironment env = new SystemEnvironment(new HashMap() {{ put("foo", "bar"); }});
        assertThat(env.getProperty("foo"), is("bar"));
    }

    @Test
    public void shouldGetSystemEnvironmentVairableWhenNoMapPassed() throws Exception{
        SystemEnvironment env = new SystemEnvironment();
        assertThat(env.getProperty("HOME"), is(System.getProperty("user.home")));
    }
}
