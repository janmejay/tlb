package com.github.tlb.ant;

import com.github.tlb.service.TalkToCruise;
import com.github.tlb.service.http.DefaultHttpAction;
import com.github.tlb.utils.SystemEnvironment;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands recording test suite time as cruise artifact
 */
public class JunitDataRecorder implements JUnitResultFormatter {
    private static final Logger logger = Logger.getLogger(JunitDataRecorder.class.getName());
    private TalkToCruise talkToCruise;

    public JunitDataRecorder(TalkToCruise talkToCruise) {
        this.talkToCruise = talkToCruise;
    }

    public JunitDataRecorder() {//default constructor
        this(new SystemEnvironment());
    }

    private JunitDataRecorder(SystemEnvironment systemEnvironment) {
        this(new TalkToCruise(systemEnvironment, new DefaultHttpAction(systemEnvironment)));
    }

    public void startTestSuite(JUnitTest jUnitTest) throws BuildException {}

    public void endTestSuite(JUnitTest jUnitTest) throws BuildException {
        String suiteName = jUnitTest.getName();
        try {
            talkToCruise.testClassFailure(suiteName, (jUnitTest.failureCount() + jUnitTest.errorCount()) > 0);
            talkToCruise.testClassTime(suiteName, jUnitTest.getRunTime());
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("recording suite time failed for %s, gobbling exception, things may not work too well for the next run", suiteName), e);
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
