package com.github.tlb.ant;

import com.github.tlb.TlbConstants;
import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.utils.SuiteFileConvertor;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import com.github.tlb.orderer.TestOrderer;
import org.apache.tools.ant.BuildException;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;

import com.github.tlb.factory.TlbFactory;
import com.github.tlb.splitter.TestSplitterCriteria;
import com.github.tlb.utils.SystemEnvironment;
import static com.github.tlb.TlbConstants.TLB_CRITERIA;

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

        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(matchedFiles);
        suiteFiles = criteria.filterSuites(suiteFiles);
        Collections.sort(suiteFiles, orderer);
        List<TlbFileResource> matchedTlbFileResources = convertor.toTlbFileResources(suiteFiles);

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
