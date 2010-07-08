package com.github.tlb.twist;

import com.github.tlb.service.TalkToCruise;
import com.github.tlb.service.http.DefaultHttpAction;
import com.github.tlb.utils.SystemEnvironment;
import com.github.tlb.utils.XmlUtil;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.commons.io.FileUtils;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.StringReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * @understands task to publish the time taken to execute scenarios
 */
public class PublishScenarioExecutionTime extends Task {
    private String reportsDir;
    private TalkToCruise talkToCruise;
    private static final String XML_REPORT_PATH = "/xml";

    public PublishScenarioExecutionTime(TalkToCruise talkToCruise) {
        this.talkToCruise = talkToCruise;
    }

    public PublishScenarioExecutionTime() {
        this(new SystemEnvironment());
    }

    public PublishScenarioExecutionTime(SystemEnvironment systemEnvironment) {
        this(new TalkToCruise(systemEnvironment, new DefaultHttpAction(systemEnvironment)));
    }

    public void setReportsDir(String reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    public String getTaskName() {
        return "publishTestTime";
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
                talkToCruise.testClassTime(testCase.attribute("name").getText(), toSecond(testCase));
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
