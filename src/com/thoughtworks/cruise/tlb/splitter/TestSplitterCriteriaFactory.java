package com.thoughtworks.cruise.tlb.splitter;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

import java.lang.reflect.InvocationTargetException;

/**
 * @understands creating a criteria based on the class
 */
public class TestSplitterCriteriaFactory {
    public static final String COUNT = "com.thoughtworks.cruise.tlb.splitter.CountBasedTestSplitterCriteria";
    public static final String TIME = "com.thoughtworks.cruise.tlb.splitter.TimeBasedTestSplitterCriteria";

    public static TestSplitterCriteria getCriteria(String criteriaName, SystemEnvironment environment) {
        if (criteriaName == null || criteriaName.isEmpty()) {
            return TestSplitterCriteria.MATCH_ALL_FILE_SET;
        }
        try {
            Class<?> criteriaClass = Class.forName(criteriaName);
            if(!TestSplitterCriteria.class.isAssignableFrom(criteriaClass)) {
                throw new IllegalArgumentException("Class '" + criteriaName + "' does not implement TestSplitterCriteria");
            }
            return getCriteria((Class<TestSplitterCriteria>) criteriaClass, environment);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to locate Criteria class '" + criteriaName + "'");
        }
    }

    static TestSplitterCriteria getCriteria(Class<? extends TestSplitterCriteria> criteriaClass, SystemEnvironment environment) {
        try {
            TestSplitterCriteria criteria = criteriaClass.getConstructor(SystemEnvironment.class).newInstance(environment);
            if (TalksToCruise.class.isInstance(criteria)) {
                TalkToCruise cruise = new TalkToCruise(environment, new DefaultHttpAction(environment));
                ((TalksToCruise)criteria).talksToCruise(cruise);
            }
            return criteria;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Public constructor matching " + criteriaClass.getName() + "(SystemEnvironment) was not found");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Public constructor matching " + criteriaClass.getName() + "(SystemEnvironment) was not found");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to create abstract class " + criteriaClass.getName());
        }
    }
}
