package com.github.tlb.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @understands result of executing a test suite
 */
public class SuiteResultEntry implements SuiteLevelEntry {

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
            if (resultLine.trim().length() > 0) entries.add(parseSingleEntry(resultLine));
        }
        return entries;
    }

    public static SuiteResultEntry parseSingleEntry(String resultLine) {
        Matcher matcher = RESULT_PATTERN.matcher(resultLine);
        SuiteResultEntry entry = null;
        if(matcher.matches()) {
            entry = new SuiteResultEntry(matcher.group(1), Boolean.parseBoolean(matcher.group(2)));
        } else {
            throw new IllegalArgumentException(String.format("failed to parse '%s' as %s", resultLine, SuiteResultEntry.class.getSimpleName()));
        }
        return entry;
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
        return toString() + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuiteResultEntry that = (SuiteResultEntry) o;

        if (failed != that.failed) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (failed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, failed);
    }

    public static List<SuiteResultEntry> parse(String buffer) {
        return parse(Arrays.asList(buffer.split("\n")));
    }
}
