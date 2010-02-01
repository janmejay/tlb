package com.thoughtworks.cruise.tlb.ant;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;

public class JunitTimeRecorderTest {
    @Test
    public void shouldCaptureAndPutTestTime() throws Exception{
        TalkToCruise talkToCruise = mock(TalkToCruise.class);
        JunitTimeRecorder recorder = new JunitTimeRecorder(talkToCruise);
        JUnitTest test = new JUnitTest("com.thoughtworks.tlb.TestWorks");
        recorder.startTestSuite(test);
        test.setRunTime(11);
        recorder.endTestSuite(test);
        verify(talkToCruise).testClassTime("com.thoughtworks.tlb.TestWorks", 11L);
    }
}
