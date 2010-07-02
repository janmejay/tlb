package com.github.tlb.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * @understands
 */
public interface EntryRepo<I,O> {
    List<O> list();

    void add(I entry);

    void diskDump(ObjectOutputStream objectOutputStream) throws IOException;

    void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException;
}
