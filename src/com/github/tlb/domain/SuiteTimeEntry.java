package com.github.tlb.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @understands time talken to execute a test suite
 */
public class SuiteTimeEntry {
    private String name;
    private long time;
    public static final Pattern SUITE_TIME_STRING = Pattern.compile("(.*?):\\s*(\\d+)");

    public SuiteTimeEntry(String name, long time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public static List<SuiteTimeEntry> parse(String buffer) {
        return parse(Arrays.asList(buffer.split("\n")));
    }

    public static String dump(List<SuiteTimeEntry> entries) {
        StringBuffer buffer = new StringBuffer();
        for (SuiteTimeEntry entry : entries) {
            buffer.append(entry.dump());
        }
        return buffer.toString();
    }

    public String dump() {
        return String.format("%s: %s\n", name, time);
    }

    public static List<SuiteTimeEntry> parse(List<String> listOfStrings) {
        List<SuiteTimeEntry> parsed = new ArrayList<SuiteTimeEntry>();
        for (String entryString : listOfStrings) {
            Matcher matcher = SUITE_TIME_STRING.matcher(entryString);
            if (matcher.matches()) parsed.add(new SuiteTimeEntry(matcher.group(1), Integer.parseInt(matcher.group(2))));
        }
        return parsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuiteTimeEntry that = (SuiteTimeEntry) o;

        if (time != that.time) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }
}
