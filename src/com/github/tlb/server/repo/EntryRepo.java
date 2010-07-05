package com.github.tlb.server.repo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * @understands storage and retrieval of records 
 */
public interface EntryRepo<I,O> {
    Collection<O> list();

    Collection<O> list(String version) throws IOException, ClassNotFoundException;

    void update(I entry);

    void diskDump(ObjectOutputStream objectOutputStream) throws IOException;

    void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException;

    void add(I entry);

    void setFactory(EntryRepoFactory factory);

    void setNamespace(String namespace);
}
