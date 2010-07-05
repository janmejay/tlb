package com.github.tlb.server.repo;

import com.github.tlb.domain.Entry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * @understands storage and retrieval of records 
 */
public interface EntryRepo<T extends Entry> {
    Collection<T> list();

    Collection<T> list(String version) throws IOException, ClassNotFoundException;

    void update(T entry);

    void diskDump(ObjectOutputStream objectOutputStream) throws IOException;

    void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException;

    void add(T entry);

    void setFactory(EntryRepoFactory factory);

    void setNamespace(String namespace);
}
