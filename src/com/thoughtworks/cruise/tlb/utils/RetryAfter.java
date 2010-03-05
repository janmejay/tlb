package com.thoughtworks.cruise.tlb.utils;

import org.jaxen.util.SingletonList;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.beans.beancontext.BeanContextMembershipEvent;

/**
 * @understands catching exception and retrying
 */
public class RetryAfter {
    private static final Log LOG = LogFactory.getLog(RetryAfter.class);

    private List<Integer> intervals = new ArrayList<Integer>(new SingletonList(0));

    public RetryAfter(int ... intervals) {
        for (int interval : intervals) {
            this.intervals.add(interval);
        }
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    public static interface Fn<T> {
        public T fn() throws Exception;
    }

    public <T> T tryFn(Fn<T> fn) {
        Exception raisedException = null;
        List<String> messages = new ArrayList<String>();
        for (int interval : intervals) {
            try {
                Thread.sleep(interval);
                return fn.fn();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
                messages.add(e.getMessage());
                raisedException = e;
            }
        }
        String message = String.format("Exausted reattempts, tried %s times, failed with messages %s at the interval of %s mills.",
                intervals.size(), messages, intervals);
        LOG.fatal(message);
        throw new RuntimeException(message);
    }
}
