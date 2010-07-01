package com.github.tlb.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @understands result of executing a test suite
 */
public class SuiteResultEntry implements Entry {

    private static final Pattern RESULT_PATTERN = Pattern.compile("(.*?):\\s*(true|false)");

    private String name;
    private boolean failed;

    public SuiteResultEntry(String name, boolean failed) {
        this.name = name;
        this.failed = failed;
    }

    public String getName() {
        return name;
    }

    public boolean hasFailed() {
        return failed;
    }

    public static List<SuiteResultEntry> parse(List<String> buffer) {
        ArrayList<SuiteResultEntry> entries = new ArrayList<SuiteResultEntry>();
        for (String resultLine : buffer) {
            Matcher matcher = RESULT_PATTERN.matcher(resultLine);
            if(matcher.matches()) entries.add(new SuiteResultEntry(matcher.group(1), Boolean.parseBoolean(matcher.group(2))));
        }
        return entries;
    }

    public static String dumpFailures(List<SuiteResultEntry> list) {
        StringBuffer buffer = new StringBuffer();
        for (SuiteResultEntry resultEntry : list) {
            if(resultEntry.hasFailed()) {
                buffer.append(resultEntry.getName() + "\n");
            }
        }
        return buffer.toString();
    }

    public static List<SuiteResultEntry> parseFailures(String buffer) {
        ArrayList<SuiteResultEntry> entries = new ArrayList<SuiteResultEntry>();
        String[] failureEntryStrings = buffer.split("\n");
        for (String failureEntryString : failureEntryStrings) {
            entries.add(new SuiteResultEntry(failureEntryString, true));
        }
        return entries;
    }

    public String dump() {
        return String.format("%s: %s\n", name, failed);
    }
}
