package com.thoughtworks.cruise.tlb.splitter;

import org.apache.tools.ant.types.resources.FileResource;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands the criteria for splitting a given test suite
 */
public abstract class TestSplitterCriteria implements TalksToCruise {
    public static TestSplitterCriteria MATCH_ALL_FILE_SET = new TestSplitterCriteria(null) {
        public List<FileResource> filter(List<FileResource> files) {
            return files;
        }
    };
    protected TalkToCruise talkToCruise;
    protected final SystemEnvironment env;
    private static final String INT = "\\d+";
    private static final Pattern NUMBER_BASED_LOAD_BALANCED_JOB = Pattern.compile("(.*?)-(" + INT + ")");
    private static final String HEX = "[a-fA-F0-9]";
    private static final String UUID = HEX + "{8}-" + HEX + "{4}-" + HEX + "{4}-" + HEX + "{4}-" + HEX + "{12}";
    private static final Pattern UUID_BASED_LOAD_BALANCED_JOB = Pattern.compile("(.*?)-(" + UUID + ")");

    public TestSplitterCriteria(SystemEnvironment env) {
        this.env = env;
    }

    public TestSplitterCriteria(TalkToCruise talkToCruise, SystemEnvironment env) {
        this(env);
        talksToCruise(talkToCruise);
    }

    public abstract List<FileResource> filter(List<FileResource> files);

    public void talksToCruise(TalkToCruise cruise) {
       this.talkToCruise = cruise;
    }

    protected List<String> jobsInTheSameFamily(List<String> jobs) {
        List<String> family = new ArrayList<String>();
        Pattern pattern = getMatcher();
        for (String job : jobs) {
            if (pattern.matcher(job).matches()) {
                family.add(job);
            }
        }
        return family;
    }

    private Pattern getMatcher() {
        return Pattern.compile(String.format("^%s-(" + INT + "|" + UUID + ")$", jobBaseName()));
    }

    private String jobBaseName() {
        Matcher matcher = NUMBER_BASED_LOAD_BALANCED_JOB.matcher(jobName());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = UUID_BASED_LOAD_BALANCED_JOB.matcher(jobName());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return jobName();
    }

    protected boolean isLast(List<String> jobs, int index) {
        return (index + 1) == jobs.size();
    }

    protected boolean isFirst(int index) {
        return (index == 0);
    }

    protected String jobName() {
        return env.getProperty(TlbConstants.CRUISE_JOB_NAME);
    }

    protected List<String> pearJobs() {
        List<String> jobs = jobsInTheSameFamily(talkToCruise.getJobs());
        Collections.sort(jobs);
        return jobs;
    }
}
