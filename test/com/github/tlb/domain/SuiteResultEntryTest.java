package com.github.tlb.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SuiteResultEntryTest {
    @Test
    public void shouldParseSingleResultString() {
        SuiteResultEntry entry = SuiteResultEntry.parseSingleEntry("com.thoughtworks.foo.FooBarTest: true");
        assertThat(entry.getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entry.hasFailed(), is(true));
        SuiteResultEntry anotherEntry = SuiteResultEntry.parseSingleEntry("com.thoughtworks.foo.BarBazTest: false");
        assertThat(anotherEntry.getName(), is("com.thoughtworks.foo.BarBazTest"));
        assertThat(anotherEntry.hasFailed(), is(false));
    }
    
    @Test
    public void shouldBombWhenFailsToParseSingleResultString() {
        try {
            SuiteResultEntry.parseSingleEntry("foo.bar.Test= true");
            fail("should have bombed for bad entry");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("failed to parse 'foo.bar.Test= true' as SuiteResultEntry"));
        }
    }

    @Test
    public void shouldParseFailuresFromString() {
        String testResultsString = "com.thoughtworks.foo.FooBarTest\ncom.thoughtworks.quux.QuuxTest";
        List<SuiteResultEntry> entries = SuiteResultEntry.parseFailures(testResultsString);
        assertThat(entries.get(0).getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entries.get(0).hasFailed(), is(true));
        assertThat(entries.get(1).getName(), is("com.thoughtworks.quux.QuuxTest"));
        assertThat(entries.get(1).hasFailed(), is(true));
        assertThat(entries.size(), is(2));
    }

    @Test
    public void shouldParseItselfFromListOfStrings() {
        List<String> listOfStrings = Arrays.asList("com.thoughtworks.foo.FooBarTest: true", "com.thoughtworks.hello.HelloWorldTest: false", "com.thoughtworks.quux.QuuxTest: true");
        List<SuiteResultEntry> entries = SuiteResultEntry.parse(listOfStrings);
        assertThat(entries.get(0).getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entries.get(0).hasFailed(), is(true));
        assertThat(entries.get(1).getName(), is("com.thoughtworks.hello.HelloWorldTest"));
        assertThat(entries.get(1).hasFailed(), is(false));
        assertThat(entries.get(2).getName(), is("com.thoughtworks.quux.QuuxTest"));
        assertThat(entries.get(2).hasFailed(), is(true));
    }

    @Test
    public void shouldDumpStringFromListOfEntries() {
        List<SuiteResultEntry> list = new ArrayList<SuiteResultEntry>();
        list.add(new SuiteResultEntry("com.thoughtworks.foo.FooBarTest", true));
        list.add(new SuiteResultEntry("com.thoughtworks.hello.HelloWorldTest", false));
        list.add(new SuiteResultEntry("com.thoughtworks.quux.QuuxTest", true));
        assertThat(SuiteResultEntry.dumpFailures(list), is("com.thoughtworks.foo.FooBarTest\ncom.thoughtworks.quux.QuuxTest\n"));
    }

    @Test
    public void shouldDumpIndividualEntry() {
        SuiteResultEntry entry = new SuiteResultEntry("com.thoughtworks.foo.FooBarTest", true);
        assertThat(entry.dump(), is("com.thoughtworks.foo.FooBarTest: true\n"));
        entry = new SuiteResultEntry("com.thoughtworks.foo.FooBarTest", false);
        assertThat(entry.dump(), is("com.thoughtworks.foo.FooBarTest: false\n"));
    }
    
    @Test
    public void shouldReturnDumpAsToString() {
        SuiteResultEntry entry = new SuiteResultEntry("foo.bar.Baz", true);
        assertThat(entry.toString(), is("foo.bar.Baz: true"));
    }
}