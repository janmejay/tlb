package com.thoughtworks.cruise.tlb.orderer;

import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.splitter.TalksToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.utils.FileUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * @understands ordering to bring failed tests first
 */
public class FailedFirstOrderer extends TestOrderer implements TalksToCruise {
    private TalkToCruise toCruise;
    private List<String> failedTestFiles;
    private FileUtil fileUtil;

    public FailedFirstOrderer(SystemEnvironment environment) {
        super(environment);
        fileUtil = new FileUtil(environment);
    }

    public int compare(TlbFileResource o1, TlbFileResource o2) {
        if (failedTestFiles == null) {
            failedTestFiles = new ArrayList<String>();
            for (String failedTestClass : toCruise.getLastRunFailedTests(toCruise.pearJobs())) {
                failedTestFiles.add(fileUtil.classFileRelativePath(failedTestClass));
            }
        }
        if (failedTestFiles.contains(o1.getName()))
            return -1;
        if (failedTestFiles.contains(o2.getName()))
            return 1;
        return 0;
    }

    public void talksToCruise(TalkToCruise cruise) {
        toCruise = cruise;
    }
}
