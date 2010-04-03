package com.github.tlb.ant;

import com.github.tlb.TestUtil;
import org.junit.Test;
import org.junit.Before;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import static org.mockito.Mockito.*;
import com.github.tlb.service.TalkToCruise;

public class JunitDataRecorderTest {
    private TalkToCruise talkToCruise;
    private JunitDataRecorder recorder;
    private TestUtil.LogFixture logFixture;
    private JUnitTest test;

    @Before
    public void setUp() {
        logFixture = new TestUtil.LogFixture();
        test = testSuite("com.thoughtworks.tlb.TestWorks", 0l, 0l, 11l);
        talkToCruise = mock(TalkToCruise.class);
        recorder = new JunitDataRecorder(talkToCruise);
        recorder.startTestSuite(test);
    }

    @Test
    public void shouldCaptureAndPutTestTime() throws Exception {
        recorder.endTestSuite(test);
        verify(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
    }

    @Test
    public void shouldCaptureAndPublishFailures() throws Exception {
        recorder.endTestSuite(test);
        verify(talkToCruise).testClassFailure("com.thoughtworks.tlb.TestWorks", false);

        recorder.endTestSuite(testSuite("com.toughtworks.FailedTest", 1l, 0l, 10l));
        verify(talkToCruise).testClassFailure("com.toughtworks.FailedTest", true);

        recorder.endTestSuite(testSuite("com.toughtworks.ErroredTest", 0l, 1l, 10l));
        verify(talkToCruise).testClassFailure("com.toughtworks.ErroredTest", true);
    }
    
    @Test
    public void shouldNotBubbleExceptionsUpWhileReportingTestTimeAsItBringsBuildsToGrindingHalt() throws Exception{
        RuntimeException errorOnTimePosting = new RuntimeException("ouch! that hurt");
        doThrow(errorOnTimePosting).when(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
        logFixture.startListening();
        recorder.endTestSuite(test);
        logFixture.assertHeard("recording suite time failed for com.thoughtworks.tlb.TestWorks, gobbling exception, things may not work too well for the next run");
    }

    @Test
    public void shouldNotBubbleExceptionsUpWhileReportingTestFailuresAsItBringsBuildsToGrindingHalt() throws Exception{
        RuntimeException errorOnTimePosting = new RuntimeException("ouch! that hurt");
        doThrow(errorOnTimePosting).when(talkToCruise).testClassFailure("com.thoughtworks.tlb.TestWorks", false);
        logFixture.startListening();
        recorder.endTestSuite(test);
        logFixture.assertHeard("recording suite time failed for com.thoughtworks.tlb.TestWorks, gobbling exception, things may not work too well for the next run");
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
