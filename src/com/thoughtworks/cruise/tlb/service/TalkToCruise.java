package com.thoughtworks.cruise.tlb.service;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static com.thoughtworks.cruise.tlb.TlbConstants.*;

import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;

/**
 * ADD an UNDERSTANDS
 */
public class TalkToCruise {
    private final SystemEnvironment environment;
    private final HttpAction httpAction;
    private static final String JOB_NAME = "name";

    public TalkToCruise(SystemEnvironment environment, HttpAction httpAction) {
        this.environment = environment;
        this.httpAction = httpAction;
    }

    public List<String> getJobs() {
        String url = stageUrl();
        Element stage = rootFor(url);
        List<Attribute> jobLinks = stage.selectNodes("//jobs/job/@href");
        ArrayList<String> jobNames = new ArrayList<String>();
        for (Attribute jobLink : jobLinks) {
            jobNames.add(rootFor(jobLink.getValue()).attributeValue(JOB_NAME));
        }
        return jobNames;
    }

    private Element rootFor(String url) {
        SAXReader builder = new SAXReader();
        String xmlString = httpAction.get(url);
        try {
            Document dom = builder.read(new StringReader(xmlString));
            return dom.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("XML could not be understood -> " + xmlString, e);
        }
    }

    private String stageUrl() {
        return String.format("%s/pipelines/%s/%s/%s/%s.xml", p(CRUISE_SERVER_URL), p(CRUISE_PIPELINE_NAME), p(CRUISE_PIPELINE_COUNTER), p(CRUISE_STAGE_NAME), p(CRUISE_STAGE_COUNTER));
    }

    private String p(String key) {
        return environment.getProperty(key);
    }
}
