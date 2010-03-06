package com.thoughtworks.cruise.tlb.ant;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.BuildException;

import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.AssertionFailedError;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands recording test suite time as cruise artifact
 */
public class JunitDataRecorder implements JUnitResultFormatter {
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
        talkToCruise.testClassTime(jUnitTest.getName(), jUnitTest.getRunTime());
    }

    public void setOutput(OutputStream outputStream) {}

    public void setSystemOutput(String s) {}

    public void setSystemError(String s) {}

    public void addError(Test test, Throwable throwable) {}

    public void addFailure(Test test, AssertionFailedError assertionFailedError) {}

    public void endTest(Test test) {}

    public void startTest(Test test) {}
}
