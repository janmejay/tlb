package com.github.tlb.domain;

import java.util.GregorianCalendar;

/**
 * @understands system time
 */
public class TimeProvider {
    public GregorianCalendar now() {
        return new GregorianCalendar();
    }
}
