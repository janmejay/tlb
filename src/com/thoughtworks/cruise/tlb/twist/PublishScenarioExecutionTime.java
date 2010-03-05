package com.thoughtworks.cruise.tlb.twist;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
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
    @SuppressWarnings("unchecked")
    public void execute() throws BuildException {
        Iterator<File> reports = FileUtils.iterateFiles(new File(reportsDir + XML_REPORT_PATH), null, false);
        while(reports.hasNext()) {
            File report = reports.next();
            try {
                Element element = rootFor(FileUtils.readFileToString(report));
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

    //TODO: Fix this duplication. TalkToCruise has the same method
    public Element rootFor(String contents) {
        SAXReader builder = new SAXReader();
        try {
            Document dom = builder.read(new StringReader(contents));
            return dom.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("XML could not be understood -> " + contents, e);
        }
    }


}
