package com.thoughtworks.cruise.tlb;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteriaFactory;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.TlbConstants.TLB_CRITERIA;

/**
 * @understands splitting Junit test classes into groups
 */
public class LoadBalancedFileSet extends FileSet {
    private final TestSplitterCriteria criteria;

    public LoadBalancedFileSet(TestSplitterCriteria criteria) {
        this.criteria = criteria;
    }

    public LoadBalancedFileSet(SystemEnvironment systemEnvironment) {
        this(TestSplitterCriteriaFactory.getCriteria(systemEnvironment.getProperty(TLB_CRITERIA), systemEnvironment));
    }

    public LoadBalancedFileSet() {
        this(new SystemEnvironment());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator iterator() {
        Iterator<FileResource> files = (Iterator<FileResource>) super.iterator();
        List<FileResource> matchedFiles = new ArrayList<FileResource>();
        while (files.hasNext()) {
            matchedFiles.add(files.next());
        }
        return criteria.filter(matchedFiles).iterator();
    }

    public TestSplitterCriteria getSplitterCriteria() {
        return criteria;
    }
}
