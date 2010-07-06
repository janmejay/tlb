package com.github.tlb.server.repo;

import com.github.tlb.domain.SuiteLevelEntry;
import com.github.tlb.domain.TimeProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @understands versions of entry list
 */
public abstract class VersioningEntryRepo<T extends SuiteLevelEntry> extends SuiteEntryRepo<T> {
    private boolean loadedData = false;
    private Map<String, VersioningEntryRepo<T>> versions;
    private final TimeProvider timeProvider;
    private GregorianCalendar createdAt;

    public VersioningEntryRepo(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        createdAt = timeProvider.now();
        versions = new ConcurrentHashMap<String, VersioningEntryRepo<T>>();
    }

    public void purgeOldVersions(int versionLifeInDays) throws IOException {
        final GregorianCalendar cal = timeProvider.now();
        cal.add(GregorianCalendar.DAY_OF_WEEK, -versionLifeInDays);//this should be parametrized
        final Date yesterday = cal.getTime();
        for (String versionKeys : versions.keySet()) {
            final VersioningEntryRepo<T> version = versions.get(versionKeys);
            if (version.createdAt.getTime().before(yesterday)) {
                versions.remove(versionKeys);
                factory.purge(version.identifier);
            }
        }
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
