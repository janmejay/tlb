package com.github.tlb.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @understands storage and retrival of size of subset of total suites run by job
 */
public class SubsetSizeRepo implements EntryRepo<String, Integer> {
    private ArrayList<Integer> entries;

    public SubsetSizeRepo() {
        entries = new ArrayList<Integer>();
    }

    public Collection<Integer> list() {
        return entries;
    }

    public void add(String entry) {
        entries.add(Integer.parseInt(entry));
    }

    public void diskDump(ObjectOutputStream outStream) throws IOException {
        outStream.writeObject(entries);
    }

    public void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        entries = (ArrayList<Integer>) inStream.readObject();
    }
}
