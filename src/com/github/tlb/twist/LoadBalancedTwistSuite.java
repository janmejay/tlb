package com.github.tlb.twist;

import com.github.tlb.TlbFileResource;
import com.github.tlb.utils.FileUtil;
import static com.github.tlb.utils.FileUtil.toFileList;
import static com.github.tlb.utils.FileUtil.stripExtension;
import com.github.tlb.splitter.TestSplitterCriteria;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.iterateFiles;

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
        File folder = new File(scenariosFolder);
        Iterator<File> collection = iterateFiles(folder, new String[] { "scn" }, false);
        List<TlbFileResource> resources = convertToTlbResource(collection);
        List<TlbFileResource> filtered = criteria.filter(resources);
        copyFilteredResources(destinationLocation, filtered);
        copyAssociatedCSVResources(folder, new File(destinationLocation));
    }

    private void copyAssociatedCSVResources(File scenarioFolder, File destination) {
        List<File> csvs = toFileList(iterateFiles(scenarioFolder, new String[]{"csv"}, false));
        List<File> filteredScns = toFileList(iterateFiles(destination, null, false));
        for (File csv : csvs) {
            for (File filteredScn : filteredScns) {
                if (stripExtension(filteredScn.getName()).equals(stripExtension(csv.getName()))) {
                    try {
                        FileUtils.copyFileToDirectory(csv, destination, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
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
