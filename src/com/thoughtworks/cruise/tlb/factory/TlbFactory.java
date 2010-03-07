package com.thoughtworks.cruise.tlb.factory;

import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.service.http.DefaultHttpAction;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import com.thoughtworks.cruise.tlb.splitter.TalksToCruise;
import com.thoughtworks.cruise.tlb.splitter.JobFamilyAwareSplitterCriteria;
import com.thoughtworks.cruise.tlb.orderer.TestOrderer;

import java.lang.reflect.InvocationTargetException;

/**
 * @understands creating a criteria based on the class
 */
public class TlbFactory<T> {
    public static final String COUNT = "com.thoughtworks.cruise.tlb.splitter.CountBasedTestSplitterCriteria";
    public static final String TIME = "com.thoughtworks.cruise.tlb.splitter.TimeBasedTestSplitterCriteria";
    public static final String FAILED_FIRST = "com.thoughtworks.cruise.tlb.orderer.FailedFirstOrderer";
    private Class<T> klass;
    private T defaultValue;
    private static TlbFactory<TestSplitterCriteria> criteriaFactory;
    private static TlbFactory<TestOrderer> testOrderer;

    TlbFactory(Class<T> klass, T defaultValue) {
        this.klass = klass;
        this.defaultValue = defaultValue;
    }

    public <T> T getInstance(String klassName, SystemEnvironment environment) {
        if (klassName == null || klassName.isEmpty()) {
            return (T) defaultValue;
        }
        try {
            Class<?> criteriaClass = Class.forName(klassName);
            if(!klass.isAssignableFrom(criteriaClass)) {
                throw new IllegalArgumentException("Class '" + klassName + "' does not implement TestSplitterCriteria");
            }
            return getInstance((Class<? extends T>) criteriaClass, environment);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to locate Criteria class '" + klassName + "'");
        }
    }

    <T> T getInstance(Class<? extends T> actualKlass, SystemEnvironment environment) {
        try {
            T criteria = actualKlass.getConstructor(SystemEnvironment.class).newInstance(environment);
            if (TalksToCruise.class.isInstance(criteria)) {
                TalkToCruise cruise = new TalkToCruise(environment, new DefaultHttpAction(environment));
                ((TalksToCruise)criteria).talksToCruise(cruise);
            }
            return criteria;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Public constructor matching " + actualKlass.getName() + "(SystemEnvironment) was not found");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Public constructor matching " + actualKlass.getName() + "(SystemEnvironment) was not found");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to create abstract class " + actualKlass.getName());
        }
    }

    public static TestSplitterCriteria getCriteria(String criteriaName, SystemEnvironment environment) {
        if (criteriaFactory == null)
            criteriaFactory = new TlbFactory<TestSplitterCriteria>(TestSplitterCriteria.class, JobFamilyAwareSplitterCriteria.MATCH_ALL_FILE_SET);
        return criteriaFactory.getInstance(criteriaName, environment);
    }

    public static TestOrderer getOrderer(String ordererName, SystemEnvironment environment) {
        if (testOrderer == null)
            testOrderer = new TlbFactory<TestOrderer>(TestOrderer.class, TestOrderer.NO_OP);
        return testOrderer.getInstance(ordererName, environment);
    }
}
