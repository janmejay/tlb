package com.thoughtworks.cruise.tlb.twist;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteriaFactory;
import static com.thoughtworks.cruise.tlb.TlbConstants.TLB_CRITERIA;

public class FilterScenariosTask extends Task {

    public static final String DEFAULT_TWIST_LOCATION = "tlb-balanced-filtered-twist-scenarios";

    private final LoadBalancedTwistSuite suite;
    private String scenariosFolder;
    private String destinationFolder = DEFAULT_TWIST_LOCATION;

    //Needed for ant
    public FilterScenariosTask() {
        this(new SystemEnvironment());
    }

    private FilterScenariosTask(SystemEnvironment systemEnvironment) {
        this(new LoadBalancedTwistSuite(TestSplitterCriteriaFactory.getCriteria(systemEnvironment.getProperty(TLB_CRITERIA), systemEnvironment)));
    }
    
    FilterScenariosTask(LoadBalancedTwistSuite suite) {
        this.suite = suite;
    }

    @Override
    public void execute() throws BuildException {
        suite.balance(scenariosFolder, destinationFolder);
    }

    @Override
    public String getTaskName() {
        return "filterScenarios";
    }

    public void setScenariosFolder(String scenariosFolder) {
        this.scenariosFolder = scenariosFolder;
    }

    public void setDestinationFolder(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }
}
