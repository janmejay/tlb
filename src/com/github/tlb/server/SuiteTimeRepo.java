package com.github.tlb.server;

import com.github.tlb.domain.SuiteTimeEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @understands time that each suite took to run
 */
public class SuiteTimeRepo implements EntryRepo<String, SuiteTimeEntry> {
    private Map<String, SuiteTimeEntry> suiteTimes;

    public SuiteTimeRepo() {
        suiteTimes = new ConcurrentHashMap<String, SuiteTimeEntry>();
    }

    public Collection<SuiteTimeEntry> list() {
        return suiteTimes.values();
    }

    public void add(String entry) {
        SuiteTimeEntry suiteTime = SuiteTimeEntry.parseSingleEntry(entry);
        suiteTimes.put(suiteTime.getName(), suiteTime);
    }

    public void diskDump(ObjectOutputStream outStream) throws IOException {
        outStream.writeObject(suiteTimes);
    }

    public void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        suiteTimes = (Map<String, SuiteTimeEntry>) inStream.readObject();
    }
}
