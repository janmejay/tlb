package com.thoughtworks.cruise.tlb.ant;

import org.junit.Test;
import org.junit.Before;
import org.mockito.internal.MockitoCore;
import static org.mockito.Mockito.*;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.TestUtil;

public class JunitTimeRecorderTest {
    private TestUtil.LogFixture logFixture;
    private TalkToCruise talkToCruise;
    private JunitTimeRecorder recorder;
    private JUnitTest test;

    @Before
    public void setUp() {
        logFixture = new TestUtil.LogFixture();
        talkToCruise = mock(TalkToCruise.class);
        recorder = new JunitTimeRecorder(talkToCruise);
        test = new JUnitTest("com.thoughtworks.tlb.TestWorks");
        recorder.startTestSuite(test);
        test.setRunTime(11);
    }

    @Test
    public void shouldCaptureAndPutTestTime() throws Exception{
        recorder.endTestSuite(test);
        verify(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
    }

    @Test
    public void shouldNotBubbleExceptionsUpAsItBringsBuildsToGrindingHalt() throws Exception{
        RuntimeException errorOnTimePosting = new RuntimeException("ouch! that hurt");
        doThrow(errorOnTimePosting).when(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
        logFixture.startListening();
        recorder.endTestSuite(test);
        logFixture.assertHeard("recording suite time failed for com.thoughtworks.tlb.TestWorks, gobbling exception, time balancing may not work too well for the next run");
    }
}
