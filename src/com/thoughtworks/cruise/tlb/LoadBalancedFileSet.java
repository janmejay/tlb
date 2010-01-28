package com.thoughtworks.cruise.tlb;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteriaFactory;

/**
 * @understands splitting Junit test classes into groups
 */
public class LoadBalancedFileSet extends FileSet {
    private final TestSplitterCriteria criteria;
    protected static final String TLB_CRITERIA = "tlb.criteria";

    public LoadBalancedFileSet(TestSplitterCriteria criteria) {
        this.criteria = criteria;
    }

    public LoadBalancedFileSet() {
        this(TestSplitterCriteriaFactory.getCriteria(System.getProperty(TLB_CRITERIA)));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator iterator() {
        Iterator<FileResource> files = super.iterator();
        ArrayList<FileResource> matchedFiles = new ArrayList<FileResource>();
        while (files.hasNext()) {
            FileResource fileResource = files.next();
            if (criteria.shouldInclude(fileResource.getFile())) {
                matchedFiles.add(fileResource);
            }
        }
        return matchedFiles.iterator();
    }

    public TestSplitterCriteria getSplitterCriteria() {
        return criteria;
    }
}
