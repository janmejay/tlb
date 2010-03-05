package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.TlbConstants;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static junit.framework.Assert.fail;
import org.apache.tools.ant.Project;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultingTestSplitterCriteriaTest {
    private Project project;

    @Before
    public void setUp() throws Exception {
        project = new Project();
    }

    @Test
    public void shouldAttemptCriterionSpecifiedInOrder() throws Exception{
        TestSplitterCriteria criteria = defaultingCriteriaWith("com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria1:com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria2:com.thoughtworks.cruise.tlb.splitter.test.LastSelectingCriteria");

        TlbFileResource foo = fileResource("foo");
        TlbFileResource bar = fileResource("bar");
        List<TlbFileResource> filteredResources = criteria.filter(Arrays.asList(foo, bar));
        assertThat(filteredResources.size(), is(1));
        assertThat(filteredResources, hasItem(bar));
    }

    @Test
    public void shouldAcceptSpacesBetweenCriterionNamesSpecified() throws Exception{
        TestSplitterCriteria criteria = defaultingCriteriaWith("com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria1   :   com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria2 :   com.thoughtworks.cruise.tlb.splitter.test.LastSelectingCriteria");

        TlbFileResource foo = fileResource("foo");
        TlbFileResource bar = fileResource("bar");
        List<TlbFileResource> filteredResources = criteria.filter(Arrays.asList(foo, bar));
        assertThat(filteredResources.size(), is(1));
        assertThat(filteredResources, hasItem(bar));
    }

    private TestSplitterCriteria defaultingCriteriaWith(String criterion) {
        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put(TlbConstants.CRITERIA_DEFAULTING_ORDER, criterion);
        SystemEnvironment env = new SystemEnvironment(envMap);
        return new DefaultingTestSplitterCriteria(env);
    }

    @Test
    public void shouldBombIfNoCriteriaCanBeUsedSuccessfully() throws Exception{
        TestSplitterCriteria criteria = defaultingCriteriaWith("com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria1:com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria2");

        TlbFileResource foo = fileResource("foo");
        TlbFileResource bar = fileResource("bar");
        try {
            criteria.filter(Arrays.asList(foo, bar));
            fail("should have raised exception as no usable criteria specified");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("None of [com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria1, com.thoughtworks.cruise.tlb.splitter.test.UnusableCriteria2] could successfully split the test suites."));
        }
    }

    private JunitFileResource fileResource(String fileName) {
        return new JunitFileResource(project, fileName);
    }
}
