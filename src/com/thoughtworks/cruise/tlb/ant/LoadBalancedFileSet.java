package com.thoughtworks.cruise.tlb.ant;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.orderer.TestOrderer;
import org.apache.tools.ant.BuildException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;

import com.thoughtworks.cruise.tlb.factory.TlbFactory;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.TlbConstants.TLB_CRITERIA;

/**
 * @understands splitting Junit test classes into groups
 */
public class LoadBalancedFileSet extends FileSet {
    private final TestSplitterCriteria criteria;
    private final TestOrderer orderer;

    public LoadBalancedFileSet(TestSplitterCriteria criteria, TestOrderer orderer) {
        this.criteria = criteria;
        this.orderer = orderer;
    }

    public LoadBalancedFileSet(SystemEnvironment systemEnvironment) {
        this(TlbFactory.getCriteria(systemEnvironment.getProperty(TLB_CRITERIA), systemEnvironment),
                TlbFactory.getOrderer(systemEnvironment.getProperty(TlbConstants.TLB_ORDERER), systemEnvironment));
    }

    public LoadBalancedFileSet() {
        this(new SystemEnvironment());
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator iterator() {
        Iterator<FileResource> files = (Iterator<FileResource>) super.iterator();
        List<TlbFileResource> matchedFiles = new ArrayList<TlbFileResource>();
        while (files.hasNext()) {
            FileResource fileResource = files.next();
            matchedFiles.add(new JunitFileResource(fileResource));
        }
        List<TlbFileResource> matchedTlbFileResources = criteria.filter(matchedFiles);
        Collections.sort(matchedTlbFileResources, orderer);
        List<FileResource> matchedFileResources = new ArrayList<FileResource>();
        for (TlbFileResource matchedTlbFileResource : matchedTlbFileResources) {
            JunitFileResource fileResource = (JunitFileResource) matchedTlbFileResource;
            matchedFileResources.add(fileResource.getFileResource());
        }
        return matchedFileResources.iterator();
    }

    public TestSplitterCriteria getSplitterCriteria() {
        return criteria;
    }

    @Override
    public void setDir(File dir) throws BuildException {
        super.setDir(dir);
        criteria.setDir(dir);
    }
}
