package com.thoughtworks.cruise.tlb.ant;

import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;

public class JunitDataRecorderTest {
    private TalkToCruise talkToCruise;
    private JunitDataRecorder recorder;

    @Before
    public void setUp() {
        JUnitTest test = testSuite("com.thoughtworks.tlb.TestWorks", 0l, 0l, 11l);
        talkToCruise = mock(TalkToCruise.class);
        recorder = new JunitDataRecorder(talkToCruise);
        recorder.endTestSuite(test);
    }

    @Test
    public void shouldCaptureAndPutTestTime() throws Exception {
        verify(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
    }

    @Test
    public void shouldCaptureAndPublishFailures() throws Exception {
        verify(talkToCruise).testClassFailure("com.thoughtworks.tlb.TestWorks", false);

        recorder.endTestSuite(testSuite("com.toughtworks.FailedTest", 1l, 0l, 10l));
        verify(talkToCruise).testClassFailure("com.toughtworks.FailedTest", true);

        recorder.endTestSuite(testSuite("com.toughtworks.ErroredTest", 0l, 1l, 10l));
        verify(talkToCruise).testClassFailure("com.toughtworks.ErroredTest", true);
    }

    private JUnitTest testSuite(String testName, long failureCount, long errorCount, long runTime) {
        JUnitTest failedTest = mock(JUnitTest.class);
        when(failedTest.getName()).thenReturn(testName);
        when(failedTest.failureCount()).thenReturn(failureCount);
        when(failedTest.errorCount()).thenReturn(errorCount);
        when(failedTest.getRunTime()).thenReturn(runTime);
        return failedTest;
    }
}
