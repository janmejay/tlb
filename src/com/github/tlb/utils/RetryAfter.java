package com.github.tlb.utils;

import org.jaxen.util.SingletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands catching exception and retrying
 */
public class RetryAfter {
    private static final Logger logger = Logger.getLogger(RetryAfter.class.getName());

    private List<Integer> intervals = new ArrayList<Integer>(new SingletonList(0));

    public RetryAfter(int ... intervals) {
        for (int interval : intervals) {
            this.intervals.add(interval);
        }
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    public static int[] seq(int value, int times) {
        int[] seq = new int[times];
        for(int i = 0; i < times; i++) {
            seq[i] = value;
        }
        return seq;
    }


    public static interface Fn<T> {
        public T fn() throws Exception;
    }

    public <T> T tryFn(Fn<T> fn) {
        List<String> messages = new ArrayList<String>();
        Exception lastException = null;
        for (int interval : intervals) {
            try {
                Thread.sleep(interval);
                return fn.fn();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                lastException = e;
                logger.log(Level.INFO, "(Re)attempt failed", lastException);
                messages.add(lastException.getMessage());
            }
        }
        String message = String.format("Exausted reattempts, tried %s times, failed with messages %s at the interval of %s mills.", intervals.size(), messages, intervals);
        logger.log(Level.WARNING, message, lastException);
        throw new RuntimeException(message, lastException);
    }
}
