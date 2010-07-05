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
public abstract class SuiteEntryRepo<T extends SuiteLevelEntry> implements EntryRepo<String, T> {
    protected Map<String, T> suiteData;
    protected String namespace;
    protected EntryRepoFactory factory;

    protected abstract T getEntry(String record);

    public SuiteEntryRepo() {
        super();
        suiteData = new ConcurrentHashMap<String, T>();
    }

    public Collection<T> list() {
        return suiteData.values();
    }

    public void update(String record) {
        T entry = getEntry(record);
        suiteData.put(entry.getName(), entry);
    }

    public final void add(String entry) {
        throw new UnsupportedOperationException("add not allowed on repository");
    }

    public final void setFactory(EntryRepoFactory factory) {
        this.factory = factory;
    }

    public final void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public final void diskDump(ObjectOutputStream outStream) throws IOException {
        outStream.writeObject(suiteData);
    }

    public void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        suiteData = (Map<String,T>) inStream.readObject();
    }
}
