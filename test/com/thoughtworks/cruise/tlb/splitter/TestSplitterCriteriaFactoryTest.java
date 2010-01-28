package com.thoughtworks.cruise.tlb.splitter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestSplitterCriteriaFactoryTest {

    @Test
    public void shouldReturnDefaultMatchAllCriteriaForEmpty() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(null);
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
        criteria = TestSplitterCriteriaFactory.getCriteria("");
        assertThat(criteria, is(TestSplitterCriteria.MATCH_ALL_FILE_SET));
    }

    @Test
    public void shouldThrowAnExceptionWhenTheCriteriaNameIsUnknown() {
        try {
            TestSplitterCriteriaFactory.getCriteria("pavan & jj");
            fail("should not be able to create random criteria!");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Not sure what you mean by criteria 'pavan & jj'"));
        }
    }

    @Test
    public void shouldReturnCountBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.COUNT);
        assertThat(criteria, instanceOf(CountBasedTestSplitterCriteria.class));
    }

    @Test
    public void shouldReturnTimeBasedCriteria() {
        TestSplitterCriteria criteria = TestSplitterCriteriaFactory.getCriteria(TestSplitterCriteriaFactory.TIME);
        assertThat(criteria, instanceOf(TimeBasedTestSplitterCriteria.class));
    }
}
