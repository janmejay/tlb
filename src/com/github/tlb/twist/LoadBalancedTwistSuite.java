package com.github.tlb.twist;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.splitter.TestSplitterCriteria;
import com.github.tlb.utils.SuiteFileConvertor;
import static com.github.tlb.utils.FileUtil.toFileList;
import static com.github.tlb.utils.FileUtil.stripExtension;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.iterateFiles;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @understands splitting Twist scenarios into groups:
 */
@SuppressWarnings("unchecked")
public class LoadBalancedTwistSuite {
    private static final Logger logger = Logger.getLogger(LoadBalancedTwistSuite.class.getName());

    private TestSplitterCriteria criteria;

    public LoadBalancedTwistSuite(TestSplitterCriteria criteria) {
        this.criteria = criteria;
    }

    public void balance(String scenariosFolder, String destinationLocation) {
        File folder = new File(scenariosFolder);
        Iterator<File> collection = iterateFiles(folder, new String[] { "scn" }, false);
        List<TlbFileResource> resources = convertToTlbResource(collection);
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        List<TlbFileResource> filtered = convertor.toTlbFileResources(criteria.filterSuites(suiteFiles));
        copyFilteredResources(destinationLocation, filtered);
        copyAssociatedCSVResources(folder, new File(destinationLocation));
    }

    private void copyAssociatedCSVResources(File scenarioFolder, File destination) {
        List<File> csvs = toFileList(iterateFiles(scenarioFolder, new String[]{"csv"}, false));
        List<File> filteredScns = toFileList(iterateFiles(destination, new String[] { "scn" }, false));
        boolean wasAnyCSVCopied = false;
        for (File csv : csvs) {
            for (File filteredScn : filteredScns) {
                if (scenarioHasAssociatedCSV(filteredScn, csv)) {
                    copyCsv(destination, csv);
                    wasAnyCSVCopied = true;
                }
            }
        }
        logIfNotCopied(filteredScns, csvs, wasAnyCSVCopied);
    }

    private void logIfNotCopied(List<File> filteredScns, List<File> csvs, boolean wasAnyCSVCopied) {
        if (!wasAnyCSVCopied) {
            logger.info(String.format("Did not find any scenarios with associated CSVs. The scenarios are:\n%s\nThe csvs are:\n%s", filteredScns, csvs));
        }
    }

    private boolean scenarioHasAssociatedCSV(File filteredScn, File csv) {
        return stripExtension(filteredScn.getName()).equals(stripExtension(csv.getName()));
    }

    private void copyCsv(File scenario, File csv) {
        try {
            FileUtils.copyFileToDirectory(csv, scenario, true);
            logger.info(String.format("Copied csv %s for scenario %s", csv.getName(), scenario.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
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
