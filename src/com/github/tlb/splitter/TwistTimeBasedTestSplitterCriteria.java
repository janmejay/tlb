package com.github.tlb.splitter;

import com.github.tlb.service.TalkToService;
import com.github.tlb.utils.SystemEnvironment;
import com.github.tlb.domain.SuiteTimeEntry;

//TODO: Hack! Remove this once we use the cononical names for the suites.
public class TwistTimeBasedTestSplitterCriteria extends TimeBasedTestSplitterCriteria {

    public TwistTimeBasedTestSplitterCriteria(TalkToService talkToService, SystemEnvironment env) {
        super(talkToService, env);
    }

    public TwistTimeBasedTestSplitterCriteria(SystemEnvironment env) {
        super(env);
    }

    protected String suiteName(SuiteTimeEntry suiteTimeEntry) {
        return suiteTimeEntry.getName();
    }
}
