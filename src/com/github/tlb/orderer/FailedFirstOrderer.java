package com.github.tlb.orderer;

import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.service.TalkToService;
import com.github.tlb.splitter.TalksToService;
import com.github.tlb.utils.SystemEnvironment;
import com.github.tlb.utils.FileUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * @understands ordering to bring failed tests first
 */
public class FailedFirstOrderer extends TestOrderer implements TalksToService {
    private TalkToService toService;
    private List<String> failedTestFiles;
    private FileUtil fileUtil;

    public FailedFirstOrderer(SystemEnvironment environment) {
        super(environment);
        fileUtil = new FileUtil(environment);
    }

    public int compare(TlbSuiteFile o1, TlbSuiteFile o2) {
        if (failedTestFiles == null) {
            failedTestFiles = new ArrayList<String>();
            for (SuiteResultEntry failedSuiteEntry : toService.getLastRunFailedTests()) {
                failedTestFiles.add(fileUtil.classFileRelativePath(failedSuiteEntry.getName()));
            }
        }
        if (failedTestFiles.contains(o1.getName()))
            return -1;
        if (failedTestFiles.contains(o2.getName()))
            return 1;
        return 0;
    }

    public void talksToService(TalkToService service) {
        toService = service;
    }
}
