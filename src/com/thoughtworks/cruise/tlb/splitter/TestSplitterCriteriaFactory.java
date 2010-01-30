package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

/**
 * @understands creating a criteria based on the name
 */
public class TestSplitterCriteriaFactory {
    public static final String COUNT = "count";
    public static final String TIME = "time";

    public static TestSplitterCriteria getCriteria(String criteriaName, SystemEnvironment environment) {
        if (criteriaName == null || criteriaName.isEmpty()) {
            return TestSplitterCriteria.MATCH_ALL_FILE_SET;
        }
        TalkToCruise cruise = new TalkToCruise(environment, new DefaultHttpAction(environment));
        if (criteriaName.equals(COUNT)) {
            return new CountBasedTestSplitterCriteria(cruise, environment);
        }
        if (criteriaName.equals(TIME)) {
            return new TimeBasedTestSplitterCriteria();
        }
        throw new IllegalArgumentException(String.format("Not sure what you mean by criteria '%s'", criteriaName));
    }
}
