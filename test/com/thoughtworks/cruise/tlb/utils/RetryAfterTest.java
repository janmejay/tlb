package com.thoughtworks.cruise.tlb.utils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class RetryAfterTest {
    private int callCount;
    private long latestInvocationTime;
    private long testStartTime;

    @Before
    public void setUp() throws Exception {
        callCount = 0;
        testStartTime = latestInvocationTime = new Date().getTime();
    }

    @Test
    public void shouldCallImmediatelyAndRetireWhenNoIntervalsGiven() throws Exception{
        RetryAfter retry = new RetryAfter();
        String returnVal = retry.tryFn(new RetryAfter.Fn<String>() {
            public String fn() {
                latestInvocationTime = new Date().getTime();
                callCount++;
                return "Foo";
            }
        });
        assertThat(returnVal, is("Foo"));
        assertThat(callCount, is(1));
        assertWasImmediate();
    }

    private void assertWasImmediate() {
        assertTrue(latestInvocationTime - testStartTime < 500);
    }

    @Test
    public void shouldCallImmediatelyAndRetireWhenIntervalsGivenButNoException() throws Exception{
        RetryAfter retry = new RetryAfter(1000, 2000, 3000);
        String returnVal = retry.tryFn(new RetryAfter.Fn<String>() {
            public String fn() {
                callCount++;
                return "Foo";
            }
        });
        assertThat(returnVal, is("Foo"));
        assertThat(callCount, is(1));
        assertWasImmediate();
    }

    @Test
    public void shouldCallImmediatelyAndRetryWhenIntervalsGivenAndExceptionsTheFirstTime() throws Exception{
        RetryAfter retry = new RetryAfter(1000, 500, 500);
        String returnVal = retry.tryFn(new RetryAfter.Fn<String>() {
            public String fn() throws Exception {
                if (callCount++ < 1) throw new Exception("aw, crap!");
                latestInvocationTime = new Date().getTime();
                return "finally";
            }
        });

        assertThat(returnVal, is("finally"));
        assertThat(callCount, is(2));
        assertTrue(latestInvocationTime - testStartTime >= 1000);
        assertTrue(latestInvocationTime - testStartTime < 1499);
    }

    @Test
    public void shouldCallImmediatelyAndRetryWhenIntervalsGivenAndExceptionsExceptForTheLastTime() throws Exception{
        RetryAfter retry = new RetryAfter(1000, 500, 500);
        String returnVal = retry.tryFn(new RetryAfter.Fn<String>() {
            public String fn() throws Exception {
                if (callCount++ < 3) throw new Exception("aw, crap!");
                latestInvocationTime = new Date().getTime();
                return "fifth time";
            }
        });

        assertThat(returnVal, is("fifth time"));
        assertThat(callCount, is(4));
        assertTrue(String.format("lastInvocationTime - testStartTime = %s", latestInvocationTime - testStartTime), latestInvocationTime - testStartTime >= 2000);
    }

    @Test
    public void shouldAbortWhenExaustsAttemptsGettingExceptionsAndShouldRaiseTheLastOne() throws Exception{
        RetryAfter retry = new RetryAfter(1000, 500, 500);
        try {
            retry.tryFn(new RetryAfter.Fn<String>() {
                public String fn() throws Exception {
                    latestInvocationTime = new Date().getTime();
                    throw new Exception("aw, crap! -> " + ++callCount);
                }
            });
            fail("attempts should have failed with an exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Exausted reattempts, tried 4 times, failed with messages [aw, crap! -> 1, aw, crap! -> 2, aw, crap! -> 3, aw, crap! -> 4] at the interval of [0, 1000, 500, 500] mills."));
        }

        assertThat(callCount, is(4));
        assertTrue(String.format("lastInvocationTime - testStartTime = %s", latestInvocationTime - testStartTime), latestInvocationTime - testStartTime >= 2000);
    }
    
    @Test
    public void shouldExposeIntervals() throws Exception{
        RetryAfter retry = new RetryAfter(1000, 500);
        List<Integer> expectedIntervals = new ArrayList<Integer>();
        expectedIntervals.add(0);
        expectedIntervals.add(1000);
        expectedIntervals.add(500);
        assertThat(retry.getIntervals(), is(expectedIntervals));
    }

    @Test
    public void shouldGenerateSequenceOfIntervals() throws Exception{
        int[] seq = RetryAfter.seq(10, 5);
        assertThat(seq.length, is(5));
        for (int i = 0; i < seq.length; i++) {
            int value = seq[i];
            assertThat(String.format("value @ index=%s was not equal, expected 10, was %s", i, value), seq.length, is(5));
        }
    }
}
