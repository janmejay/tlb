package com.github.tlb.utils;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.util.HashMap;

public class SystemEnvironmentTest {
    
    @Test
    public void shouldGetPropertyAvailableInGivenMap() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        SystemEnvironment env = new SystemEnvironment(map);
        assertThat(env.getProperty("foo"), is("bar"));
    }

    @Test
    public void shouldGetSystemEnvironmentVairableWhenNoMapPassed() throws Exception{
        SystemEnvironment env = new SystemEnvironment();
        assertThat(env.getProperty("HOME"), is(System.getProperty("user.home")));
    }
    
    @Test
    public void shouldDefaultEnvVariableValues() {
        HashMap<String, String> map = new HashMap<String, String>();
        SystemEnvironment env = new SystemEnvironment(map);
        assertThat(env.getProperty("foo", "bar"), is("bar"));
        assertThat(env.getProperty("foo"), is(nullValue()));
        map.put("foo", "baz");
        assertThat(env.getProperty("foo", "bar"), is("baz"));
        assertThat(env.getProperty("foo"), is("baz"));
    }
}
