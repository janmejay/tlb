package com.github.tlb.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class SuiteTimeEntryTest {
    @Test
    public void shouldReturnADifferentSmoothenedEntryRatherThanMutatingItself() {
        SuiteTimeEntry entry = new SuiteTimeEntry("baz.bar.Foo", 12);
        assertThat(entry.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 18), 0.05), not(sameInstance(entry)));
    }

    @Test
    public void shouldBombIfNewDatapointIsForDifferentSuite() {
        SuiteTimeEntry entry = new SuiteTimeEntry("baz.bar.Quux", 12);
        try {
            entry.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 18), 0.05);
            fail("should have bombed for data point from a different suite");
        } catch(Exception e) {
            assertThat(e.getMessage(), is("suite baz.bar.Quux can not be smoothed with data point from baz.bar.Foo"));
        }
    }

    @Test
    public void shouldSmoothenWithRespectToAnotherEntity() {
        SuiteTimeEntry smoothened = new SuiteTimeEntry("baz.bar.Foo", 12);
        assertThat(smoothened.getTime(), is(12l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 18), 0.05);
        assertThat(smoothened.getName(), is("baz.bar.Foo"));
        assertThat(smoothened.getTime(), is(12l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 12), 0.05);
        assertThat(smoothened.getTime(), is(12l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 102), 0.05);
        assertThat(smoothened.getTime(), is(17l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 14), 0.05);
        assertThat(smoothened.getTime(), is(17l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 90), 0.05);
        assertThat(smoothened.getTime(), is(21l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 13), 0.05);
        assertThat(smoothened.getTime(), is(21l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 12), 0.05);
        assertThat(smoothened.getTime(), is(21l));
        smoothened = smoothened.smoothedWrt(new SuiteTimeEntry("baz.bar.Foo", 10), 0.05);
        assertThat(smoothened.getTime(), is(20l));
    }

    @Test
    public void shouldParseItselfFromString() {
        String testTimesString = "com.thoughtworks.foo.FooBarTest: 45\ncom.thoughtworks.hello.HelloWorldTest: 103\ncom.thoughtworks.quux.QuuxTest: 54";
        List<SuiteTimeEntry> entries = SuiteTimeEntry.parse(testTimesString);
        assertThat(entries.get(0).getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entries.get(0).getTime(), is(45l));
        assertThat(entries.get(1).getName(), is("com.thoughtworks.hello.HelloWorldTest"));
        assertThat(entries.get(1).getTime(), is(103l));
        assertThat(entries.get(2).getName(), is("com.thoughtworks.quux.QuuxTest"));
        assertThat(entries.get(2).getTime(), is(54l));
    }

    @Test
    public void shouldParseItselfFromListOfStrings() {
        List<String> listOfStrings = Arrays.asList("com.thoughtworks.foo.FooBarTest: 45", "com.thoughtworks.hello.HelloWorldTest: 103", "com.thoughtworks.quux.QuuxTest: 54");
        List<SuiteTimeEntry> entries = SuiteTimeEntry.parse(listOfStrings);
        assertThat(entries.get(0).getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entries.get(0).getTime(), is(45l));
        assertThat(entries.get(1).getName(), is("com.thoughtworks.hello.HelloWorldTest"));
        assertThat(entries.get(1).getTime(), is(103l));
        assertThat(entries.get(2).getName(), is("com.thoughtworks.quux.QuuxTest"));
        assertThat(entries.get(2).getTime(), is(54l));
    }

    @Test
    public void shouldParseItselfFromListOfStringsInspiteOfEmptyStringsInBetween() {
        List<String> listOfStrings = Arrays.asList("com.thoughtworks.foo.FooBarTest: 45", "", "com.thoughtworks.hello.HelloWorldTest: 103", "", "com.thoughtworks.quux.QuuxTest: 54");
        List<SuiteTimeEntry> entries = SuiteTimeEntry.parse(listOfStrings);
        assertThat(entries.size(), is(3));
        assertThat(entries.get(0).getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entries.get(0).getTime(), is(45l));
        assertThat(entries.get(1).getName(), is("com.thoughtworks.hello.HelloWorldTest"));
        assertThat(entries.get(1).getTime(), is(103l));
        assertThat(entries.get(2).getName(), is("com.thoughtworks.quux.QuuxTest"));
        assertThat(entries.get(2).getTime(), is(54l));
    }

    @Test
    public void shouldDumpStringFromListOfEntries() {
        List<SuiteTimeEntry> list = new ArrayList<SuiteTimeEntry>();
        list.add(new SuiteTimeEntry("com.thoughtworks.foo.FooBarTest", 45l));
        list.add(new SuiteTimeEntry("com.thoughtworks.hello.HelloWorldTest", 103l));
        list.add(new SuiteTimeEntry("com.thoughtworks.quux.QuuxTest", 54l));
        assertThat(SuiteTimeEntry.dump(list), is("com.thoughtworks.foo.FooBarTest: 45\ncom.thoughtworks.hello.HelloWorldTest: 103\ncom.thoughtworks.quux.QuuxTest: 54\n"));
    }

    @Test
    public void shouldParseItselfFromSingleString() {
        SuiteTimeEntry entry = SuiteTimeEntry.parseSingleEntry("com.thoughtworks.foo.FooBarTest: 45");
        assertThat(entry.getName(), is("com.thoughtworks.foo.FooBarTest"));
        assertThat(entry.getTime(), is(45l));
    }
    
    @Test
    public void shouldBombWhenFailsToParseItselfFromSingleString() {
        try {
            SuiteTimeEntry.parseSingleEntry("com.thoughtworks.foo.FooBarTest= 45");
            fail("should have bombed as entry string is invalid");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("failed to parse 'com.thoughtworks.foo.FooBarTest= 45' as SuiteTimeEntry"));
        }
    }

    @Test
    public void shouldReturnDumpAsToString() {
        SuiteTimeEntry entry = new SuiteTimeEntry("foo.bar.Baz", 103l);
        assertThat(entry.toString(), is("foo.bar.Baz: 103"));
    }
}
