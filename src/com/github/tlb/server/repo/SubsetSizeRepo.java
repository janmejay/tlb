package com.github.tlb.server.repo;

import com.github.tlb.domain.SubsetSizeEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @understands storage and retrival of size of subset of total suites run by job
 */
public class SubsetSizeRepo implements EntryRepo<SubsetSizeEntry> {
    private List<SubsetSizeEntry> entries;

    public SubsetSizeRepo() {
        entries = new ArrayList<SubsetSizeEntry>();
    }

    public Collection<SubsetSizeEntry> list() {
        return entries;
    }

    public Collection<SubsetSizeEntry> list(String version) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("versioning not allowed");
    }

    public void update(SubsetSizeEntry entry) {
        throw new UnsupportedOperationException("update not allowed on repository");
    }

    public void diskDump(ObjectOutputStream outStream) throws IOException {
        outStream.writeObject(entries);
    }

    public void load(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        entries = (ArrayList<SubsetSizeEntry>) inStream.readObject();
    }

    public void add(SubsetSizeEntry entry) {
        entries.add(entry);
    }

    public void setFactory(EntryRepoFactory factory) {
        //doesn't need
    }

    public void setNamespace(String namespace) {
        //doesn't need
    }
}
