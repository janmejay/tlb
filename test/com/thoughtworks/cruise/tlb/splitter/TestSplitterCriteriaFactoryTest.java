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
    private static final SystemEnvironment ENV = new SystemEnvironment(new HashMap<String, String>() {{
        put(TlbConstants.CRUISE_SERVER_URL, "http://localhost:8153/cruise");
    }});

    @Test
    public void shouldReturnDefaultMatchAllCriteriaForEmpty() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(null, ENV);
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
        criteria = TestSplitterCriteriaFactory.getCriteria("", ENV);
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaNameIsUnknown() {
        try {
            TestSplitterCriteriaFactory.getCriteria("pavan & jj", ENV);
            fail("should not be able to create random criteria!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Not sure what you mean by criteria 'pavan & jj'"));
        }
    }

    @Test
    public void shouldReturnCountBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.COUNT, ENV);
        assertThat(criteria, instanceOf(CountBasedTestSplitterCriteria.class));
    }

    @Test
    public void shouldReturnTimeBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.TIME, ENV);
        assertThat(criteria, instanceOf(TimeBasedTestSplitterCriteria.class));
    }
}
