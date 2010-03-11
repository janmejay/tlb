package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands choosing criteria in order of preference
 */
public class DefaultingTestSplitterCriteria extends TestSplitterCriteria {
    private static final Logger logger = Logger.getLogger(DefaultingTestSplitterCriteria.class.getName());

    private ArrayList<TestSplitterCriteria> criterion;

    public DefaultingTestSplitterCriteria(SystemEnvironment env) {
        super(env);
        criterion = new ArrayList<TestSplitterCriteria>();
        String[] criteriaNames = criteriaNames(env);
        for (String criteriaName : criteriaNames) {
            TestSplitterCriteria splitterCriteria = TestSplitterCriteriaFactory.getCriteria(criteriaName, env);
            criterion.add(splitterCriteria);
        }
    }

    private String[] criteriaNames(SystemEnvironment env) {
        return env.getProperty(TlbConstants.CRITERIA_DEFAULTING_ORDER).split("\\s*:\\s*");
    }

    public List<TlbFileResource> filter(List<TlbFileResource> fileResources) {
        for (TestSplitterCriteria criteria : criterion) {
            try {
                List<TlbFileResource> subset = criteria.filter(fileResources);
                logger.info(String.format("Used %s to balance.", criteria.getClass().getCanonicalName()));
                return subset;
            } catch (Exception e) {
                logger.log(Level.WARNING, String.format("Could not use %s for balancing because: %s.", criteria.getClass().getCanonicalName(), e.getMessage()), e);
                continue;
            }
        }
        throw new IllegalStateException(String.format("None of %s could successfully split the test suites.", Arrays.asList(criteriaNames(env))));
    }
}
