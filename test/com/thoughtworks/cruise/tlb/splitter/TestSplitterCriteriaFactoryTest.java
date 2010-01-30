package com.thoughtworks.cruise.tlb.splitter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.TlbConstants;

import java.util.HashMap;

public class TestSplitterCriteriaFactoryTest {

    @Test
    public void shouldReturnDefaultMatchAllCriteriaForEmpty() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(null, env());
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
        criteria = TestSplitterCriteriaFactory.getCriteria("", env());
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaNameIsUnknown() {
        try {
            TestSplitterCriteriaFactory.getCriteria("pavan & jj", env());
            fail("should not be able to create random criteria!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Not sure what you mean by criteria 'pavan & jj'"));
        }
    }

    @Test
    public void shouldReturnCountBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.COUNT, env());
        assertThat(criteria, instanceOf(CountBasedTestSplitterCriteria.class));
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
}
