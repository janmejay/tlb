package com.github.tlb.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @understands reading the environment variables of the system on which tlb runs
 */
public class SystemEnvironment {
    private static final Pattern REF = Pattern.compile(".*\\$\\{(.+?)\\}.*");

    private Map<String, String> variables;

    public SystemEnvironment(Map<String, String> variables) {
        this.variables = variables;
    }

    public SystemEnvironment() {
        this(System.getenv());
    }

    public String getProperty(String key) {
        String value = variables.get(key);
        value = substituteRefs(value);
        return value;
    }

    private String substituteRefs(String value) {
        if (value == null) return null;
        final Matcher matcher = REF.matcher(value);
        if (matcher.find()) {
            final String ref = matcher.group(1);
            return substituteRefs(value.replace(String.format("${%s}", ref), getProperty(ref)));
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }
}
