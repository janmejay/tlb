package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteLevelEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @understands versions of entry list
 */
public abstract class VersioningEntryRepo<T extends SuiteLevelEntry> extends SuiteEntryRepo<T> {
    private boolean loadedData = false;
    private Map<String, VersioningEntryRepo<T>> versions;

    protected VersioningEntryRepo() {
        versions = new ConcurrentHashMap<String, VersioningEntryRepo<T>>();
    }

    public abstract VersioningEntryRepo<T> getSubRepo(String versionIdentifier) throws IOException, ClassNotFoundException;

    public Collection<T> list(String versionIdentifier) throws IOException, ClassNotFoundException {
        VersioningEntryRepo<T> version;
        synchronized (versionIdentifier.intern()) {
            version = versions.get(versionIdentifier);
            if (version == null) {
                version = getSubRepo(versionIdentifier);
                if (!version.loadedData) {
                    for (T entry : list()) {
                        version.update(entry);
                    }
                }
                versions.put(versionIdentifier, version);
            }
        }
        return version.list();
    }

    @Override
    public final void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        super.load(inStream);
        loadedData = true;
    }
}
