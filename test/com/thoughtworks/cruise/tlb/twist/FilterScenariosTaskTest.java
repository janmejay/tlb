package com.thoughtworks.cruise.tlb.twist;

import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FilterScenariosTaskTest {
    private LoadBalancedTwistSuite twistSuite;
    private FilterScenariosTask task;

    @Before
    public void setUp() throws Exception {
        twistSuite = mock(LoadBalancedTwistSuite.class);
        task = new FilterScenariosTask(twistSuite);
    }

    @Test
    public void shouldCallLoadBalanceWithDefaultDestinationFolderIfNotSet() throws Exception {
        task.setScenariosFolder("scenarios");

        task.execute();

        verify(twistSuite).balance("scenarios", FilterScenariosTask.DEFAULT_TWIST_LOCATION);
    }

    @Test
    public void shouldCallLoadBalanceWithSetSourceAndDestinationFolder() throws Exception {
        task.setScenariosFolder("scenarios");
        task.setDestinationFolder("destination");

        task.execute();

        verify(twistSuite).balance("scenarios", "destination");
    }
}
