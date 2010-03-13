package com.thoughtworks.cruise.tlb.ant;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.BuildException;

import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.AssertionFailedError;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands recording test suite time as cruise artifact
 */
public class JunitTimeRecorder implements JUnitResultFormatter {
    private static final Logger logger = Logger.getLogger(JunitTimeRecorder.class.getName());
    private TalkToCruise talkToCruise;

    public JunitTimeRecorder(TalkToCruise talkToCruise) {
        this.talkToCruise = talkToCruise;
    }

    public JunitTimeRecorder() {//default constructor
        this(new SystemEnvironment());
    }

    private JunitTimeRecorder(SystemEnvironment systemEnvironment) {
        this(new TalkToCruise(systemEnvironment, new DefaultHttpAction(systemEnvironment)));
    }
    
    public void startTestSuite(JUnitTest jUnitTest) throws BuildException {}

    public void endTestSuite(JUnitTest jUnitTest) throws BuildException {
        String suiteName = jUnitTest.getName();
        try {
            talkToCruise.testClassTime(suiteName, jUnitTest.getRunTime());
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("recording suite time failed for %s, gobbling exception, time balancing may not work too well for the next run", suiteName), e);
        }
    }

    public void setOutput(OutputStream outputStream) {}

    public void setSystemOutput(String s) {}

    public void setSystemError(String s) {}

    public void addError(Test test, Throwable throwable) {}

    public void addFailure(Test test, AssertionFailedError assertionFailedError) {}

    public void endTest(Test test) {}

    public void startTest(Test test) {}
}
