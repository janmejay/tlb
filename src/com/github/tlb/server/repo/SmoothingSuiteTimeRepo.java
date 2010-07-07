package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteTimeEntry;
import com.github.tlb.domain.TimeProvider;

import java.io.IOException;

/**
 * @understands storage and retrieval of time that each suite took to run
 */
public class SmoothingSuiteTimeRepo extends SuiteTimeRepo {
    private final double smootheningFactor;

    public SmoothingSuiteTimeRepo(TimeProvider timeProvider, double smootheningFactor) {
        super(timeProvider);
        if (smootheningFactor < 0 || smootheningFactor > 1) {
            throw new IllegalArgumentException("smoothing factor must be a value between 0 and 1");
        }
        this.smootheningFactor = smootheningFactor;
    }

    @Override
    public void update(SuiteTimeEntry newRecord) {
        final SuiteTimeEntry toBeUpdated = suiteData.get(newRecord.getName());
        super.update(toBeUpdated == null ? newRecord : toBeUpdated.smoothedWrt(newRecord, smootheningFactor));
    }
}