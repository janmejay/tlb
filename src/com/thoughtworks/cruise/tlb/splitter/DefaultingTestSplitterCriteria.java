package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @understands choosing criteria in order of preference
 */
public class DefaultingTestSplitterCriteria extends TestSplitterCriteria {
    private static final Log LOG = LogFactory.getLog(DefaultingTestSplitterCriteria.class);

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

    public List<FileResource> filter(List<FileResource> fileResources) {
        for (TestSplitterCriteria criteria : criterion) {
            try {
                List<FileResource> subset = criteria.filter(fileResources);
                LOG.info(String.format("Used %s to balance.", criteria.getClass().getCanonicalName()));
                return subset;
            } catch (Exception e) {
                LOG.error(String.format("Could not use %s for balancing because: %s.", criteria.getClass().getCanonicalName(), e.getMessage()));
                e.printStackTrace(System.err);
                continue;
            }
        }
        throw new IllegalStateException(String.format("None of %s could successfully split the test suites.", Arrays.asList(criteriaNames(env))));
    }
}
