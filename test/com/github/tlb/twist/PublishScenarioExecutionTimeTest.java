package com.github.tlb.twist;

import com.github.tlb.TestUtil;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.github.tlb.service.TalkToCruise;

public class PublishScenarioExecutionTimeTest {

    @Test
    public void shouldPublishScenarioExecutionTimeToCruise() throws Exception {
        TalkToCruise cruise = mock(TalkToCruise.class);
        PublishScenarioExecutionTime publishTime = new PublishScenarioExecutionTime(cruise);
        String reportsDir = "reports";
        publishTime.setReportsDir(reportsDir);

        File reportsFolder = new File(reportsDir + "/xml");
        reportsFolder.mkdirs();
        populateReports(reportsFolder);

        publishTime.execute();
        verify(cruise).testClassTime("Agent UI Auto Refresh.scn", 85822);
        verify(cruise).testClassTime("AgentsApi.scn", 77871);
    }

    private void populateReports(File reportsFolder) throws Exception {
        writeToFile(reportsFolder, "TWIST_TEST--scenarios.01.xml");
        writeToFile(reportsFolder, "TWIST_TEST--scenarios.02.xml");
    }

    private void writeToFile(File reportsFolder, String name) throws IOException, URISyntaxException {
        File file = new File(reportsFolder.getAbsolutePath(), name);
        file.createNewFile();
        file.deleteOnExit();
        FileUtils.writeStringToFile(file, TestUtil.fileContents("resources/" + name));
    }
}
