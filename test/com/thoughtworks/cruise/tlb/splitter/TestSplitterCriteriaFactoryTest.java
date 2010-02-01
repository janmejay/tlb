package com.thoughtworks.cruise.tlb.splitter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.apache.tools.ant.types.resources.FileResource;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;

import java.util.HashMap;
import java.util.List;

public class TestSplitterCriteriaFactoryTest {

    @Test
    public void shouldReturnDefaultMatchAllCriteriaForEmpty() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria((String) null, env());
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
        criteria = TestSplitterCriteriaFactory.getCriteria("", env());
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaClassIsNotFound() {
        try {
            TestSplitterCriteriaFactory.getCriteria("com.thoughtworks.cruise.tlb.MissingCriteria", env());
            fail("should not be able to create random criteria!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unable to locate Criteria class 'com.thoughtworks.cruise.tlb.MissingCriteria'"));
        }
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaClassDoesNotImplementTestSplitterCriteria() {
        try {
            TestSplitterCriteriaFactory.getCriteria("java.lang.String", env());
            fail("should not be able to create criteria that doesn't implement TestSplitterCriteria");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Class 'java.lang.String' does not implement TestSplitterCriteria"));
        }
    }

    @Test
    public void shouldReturnCountBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.COUNT, env());
        assertThat(criteria, instanceOf(CountBasedTestSplitterCriteria.class));
    }

    @Test
    public void shouldInjectCruiseCommunicatorWhenImplementsTalkToCruise() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(MockCriteria.class, env());
        assertThat(criteria, instanceOf(MockCriteria.class));
        assertThat(((MockCriteria)criteria).calledTalksToCruise, is(true));
    }

    @Test
    public void shouldReturnTimeBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.TIME, env());
        assertThat(criteria, instanceOf(TimeBasedTestSplitterCriteria.class));
    }

    private SystemEnvironment env() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TlbConstants.CRUISE_SERVER_URL, "https://localhost:8154/cruise");
        return new SystemEnvironment(map);
    }

    private static class MockCriteria implements TestSplitterCriteria, TalksToCruise {
        private boolean calledFilter = false;
        private boolean calledTalksToCruise = false;

        public MockCriteria(SystemEnvironment env) { }

        public List<FileResource> filter(List<FileResource> files) {
            this.calledFilter = true;
            return null;
        }

        public void talksToCruise(TalkToCruise cruise) {
            this.calledTalksToCruise = true;
        }
    }
}
