package com.github.tlb.server.repo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @understands
 */
public interface EntryRepo<I,O> {
    Collection<O> list();

    void update(I entry);

    void diskDump(ObjectOutputStream objectOutputStream) throws IOException;

    void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException;

    void add(String entry);
}
