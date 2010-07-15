package com.github.tlb.twist;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.splitter.TestSplitterCriteria;
import com.github.tlb.utils.SuiteFileConvertor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @understands splitting Twist scenarios into groups
 */
@SuppressWarnings("unchecked")
public class LoadBalancedTwistSuite {
    private TestSplitterCriteria criteria;

    public LoadBalancedTwistSuite(TestSplitterCriteria criteria) {
        this.criteria = criteria;
    }

    public void balance(String scenariosFolder, String destinationLocation) {
        Iterator<File> collection = FileUtils.iterateFiles(new File(scenariosFolder), null, false);
        List<TlbFileResource> resources = convertToTlbResource(collection);
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        List<TlbFileResource> filtered = convertor.toTlbFileResources(criteria.filterSuites(suiteFiles));
        copyFilteredResources(destinationLocation, filtered);
    }

    private List<TlbFileResource> convertToTlbResource(Iterator<File> collection) {
        List<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        while (collection.hasNext()) {
            File file = collection.next();
            resources.add(new SceanrioFileResource(file));
        }
        return resources;
    }

    private void copyFilteredResources(String destinationLocation, List<TlbFileResource> filtered) {
        File dest = new File(destinationLocation);
        try {
            FileUtils.forceMkdir(dest);
            for (TlbFileResource tlbFileResource : filtered) {
                FileUtils.copyFileToDirectory(tlbFileResource.getFile(), dest, true);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to setup the destination location '" + destinationLocation + "' properly", e);
        }
    }
}
