package com.github.tlb.twist;

import com.github.tlb.service.TalkToService;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.service.http.DefaultHttpAction;
import com.github.tlb.utils.SystemEnvironment;
import com.github.tlb.utils.XmlUtil;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @understands task to publish the time taken to execute scenarios
 */
public class PublishScenarioExecutionTime extends Task {
    private String reportsDir;
    private TalkToService talkToService;
    private static final String XML_REPORT_PATH = "/xml";

    public PublishScenarioExecutionTime(TalkToService talkToService) {
        this.talkToService = talkToService;
    }

    public PublishScenarioExecutionTime() {
        this(new SystemEnvironment());
    }

    public PublishScenarioExecutionTime(SystemEnvironment systemEnvironment) {
        this(new TalkToCruise(systemEnvironment));
    }

    public void setReportsDir(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws BuildException {
        Iterator<File> reports = FileUtils.iterateFiles(new File(reportsDir + XML_REPORT_PATH), null, false);
        while(reports.hasNext()) {
            File report = reports.next();
            try {
                Element element = XmlUtil.domFor(FileUtils.readFileToString(report));
                Element testCase = (Element) element.selectSingleNode("//testcase");
                talkToService.testClassTime(testCase.attribute("name").getText(), toSecond(testCase));
            } catch (IOException e) {
                throw new RuntimeException("Could not read the twist report: " + report.getName(), e);
            }
        }
    }

    private long toSecond(Element testCase) {
        double time = Double.parseDouble(testCase.attribute("time").getText());
        return (long)(time * 1000);
    }
}
