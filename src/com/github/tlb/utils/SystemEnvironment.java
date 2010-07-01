package com.github.tlb.utils;

import java.util.Map;

/**
 * @understands reading the environment variables of the system on which tlb runs
 */
public class SystemEnvironment {

    private Map<String, String> variables;

    public SystemEnvironment(Map<String, String> variables) {
        this.variables = variables;
    }

    public SystemEnvironment() {
        this(System.getenv());
    }

    public String getProperty(String key) {
        return this.variables.get(key);
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }
}
