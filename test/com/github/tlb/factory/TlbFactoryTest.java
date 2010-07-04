package com.github.tlb.factory;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.github.tlb.TlbConstants;
import com.github.tlb.TlbFileResource;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.service.TalkToTlbServer;
import org.hamcrest.core.Is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.github.tlb.utils.SystemEnvironment;
import com.github.tlb.orderer.TestOrderer;
import com.github.tlb.orderer.FailedFirstOrderer;
import com.github.tlb.splitter.*;
import com.github.tlb.service.TalkToService;

import java.util.HashMap;
import java.util.List;

public class TlbFactoryTest {

    @Test
    public void shouldReturnDefaultMatchAllCriteriaForEmpty() {
        TestSplitterCriteria criteria = TlbFactory.getCriteria(null, env("com.github.tlb.service.TalkToCruise"));
        assertThat(criteria, Is.is(JobFamilyAwareSplitterCriteria.MATCH_ALL_FILE_SET));
        criteria = TlbFactory.getCriteria("", env("com.github.tlb.service.TalkToCruise"));
        assertThat(criteria, is(JobFamilyAwareSplitterCriteria.MATCH_ALL_FILE_SET));
    }
    
    @Test
    public void shouldReturnNoOPOrdererForEmpty() {
        TestOrderer orderer = TlbFactory.getOrderer(null, env("com.github.tlb.service.TalkToCruise"));
        assertThat(orderer, Is.is(TestOrderer.NO_OP));
        orderer = TlbFactory.getOrderer("", env("com.github.tlb.service.TalkToCruise"));
        assertThat(orderer, is(TestOrderer.NO_OP));
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaClassIsNotFound() {
        try {
            TlbFactory.getCriteria("com.thoughtworks.cruise.tlb.MissingCriteria", env("com.github.tlb.service.TalkToCruise"));
            fail("should not be able to create random criteria!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unable to locate Criteria class 'com.thoughtworks.cruise.tlb.MissingCriteria'"));
        }

        try {
            TlbFactory.getOrderer("com.thoughtworks.cruise.tlb.MissingOrderer", env("com.github.tlb.service.TalkToCruise"));
            fail("should not be able to create random orderer!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unable to locate Criteria class 'com.thoughtworks.cruise.tlb.MissingOrderer'"));
        }
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaClassDoesNotImplementTestSplitterCriteria() {
        try {
            TlbFactory.getCriteria("java.lang.String", env("com.github.tlb.service.TalkToCruise"));
            fail("should not be able to create criteria that doesn't implement TestSplitterCriteria");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Class 'java.lang.String' does not implement TestSplitterCriteria"));
        }
    }

    @Test
    public void shouldReturnCountBasedCriteria() {
        TestSplitterCriteria criteria = TlbFactory.getCriteria(TlbFactory.COUNT, env("com.github.tlb.service.TalkToCruise"));
        assertThat(criteria, instanceOf(CountBasedTestSplitterCriteria.class));
    }
    
    @Test
    public void shouldInjectTlbCommunicatorWhenImplementsTalkToService() {
        TlbFactory<TestSplitterCriteria> criteriaFactory = new TlbFactory<TestSplitterCriteria>(TestSplitterCriteria.class, JobFamilyAwareSplitterCriteria.MATCH_ALL_FILE_SET);
        TestSplitterCriteria criteria = criteriaFactory.getInstance(MockCriteria.class, env("com.github.tlb.service.TalkToTlbServer"));
        assertThat(criteria, instanceOf(MockCriteria.class));
        assertThat(((MockCriteria)criteria).calledTalksToService, is(true));
        assertThat(((MockCriteria)criteria).talker, is(TalkToTlbServer.class));
    }

    @Test
    public void shouldInjectCruiseCommunicatorWhenImplementsTalkToService() {
        TlbFactory<TestSplitterCriteria> criteriaFactory = new TlbFactory<TestSplitterCriteria>(TestSplitterCriteria.class, JobFamilyAwareSplitterCriteria.MATCH_ALL_FILE_SET);
        TestSplitterCriteria criteria = criteriaFactory.getInstance(MockCriteria.class, env("com.github.tlb.service.TalkToCruise"));
        assertThat(criteria, instanceOf(MockCriteria.class));
        assertThat(((MockCriteria)criteria).calledTalksToService, is(true));
        assertThat(((MockCriteria)criteria).talker, is(TalkToCruise.class));
    }

    @Test
    public void shouldReturnTimeBasedCriteria() {
        TestSplitterCriteria criteria = TlbFactory.getCriteria(TlbFactory.TIME, env("com.github.tlb.service.TalkToCruise"));
        assertThat(criteria, instanceOf(TimeBasedTestSplitterCriteria.class));
    }

    @Test
    public void shouldReturnFailedFirstOrderer() {
        TestOrderer failedTestsFirstOrderer = TlbFactory.getOrderer(TlbFactory.FAILED_FIRST, env("com.github.tlb.service.TalkToCruise"));
        assertThat(failedTestsFirstOrderer, instanceOf(FailedFirstOrderer.class));
    }

    private SystemEnvironment env(String talkToService) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TlbConstants.Cruise.CRUISE_SERVER_URL, "https://localhost:8154/cruise");
        map.put(TlbConstants.TlbServer.URL, "http://localhost:7019");
        map.put(TlbConstants.TALK_TO_SERVICE, talkToService);
        return new SystemEnvironment(map);
    }

    private static class MockCriteria extends JobFamilyAwareSplitterCriteria implements TalksToService {
        private boolean calledFilter = false;
        private boolean calledTalksToService = false;
        private TalkToService talker;

        public MockCriteria(SystemEnvironment env) {
            super(env);
        }

        protected List<TlbFileResource> subset(List<TlbFileResource> fileResources) {
            this.calledFilter = true;
            return null;
        }

        public void talksToService(TalkToService service) {
            this.calledTalksToService = true;
            this.talker = service;
        }
    }
}
