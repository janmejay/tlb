package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteLevelEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @understands persistence and retrieval of suite based data
 */
public abstract class SuiteEntryRepo<T extends SuiteLevelEntry> implements EntryRepo<String, SuiteLevelEntry> {
    private Map<String, SuiteLevelEntry> suiteData;

    protected abstract T getEntry(String record);

    public SuiteEntryRepo() {
        suiteData = new ConcurrentHashMap<String, SuiteLevelEntry>();
    }

    public Collection<SuiteLevelEntry> list() {
        return suiteData.values();
    }

    public void update(String record) {
        SuiteLevelEntry entry = getEntry(record);
        suiteData.put(entry.getName(), entry);
    }

    public final void add(String entry) {
        throw new UnsupportedOperationException("add not allowed on repository");
    }

    public void diskDump(ObjectOutputStream outStream) throws IOException {
        outStream.writeObject(suiteData);
    }

    public void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        suiteData = (Map<String, SuiteLevelEntry>) inStream.readObject();
    }
}
