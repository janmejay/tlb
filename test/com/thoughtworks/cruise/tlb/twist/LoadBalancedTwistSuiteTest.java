package com.thoughtworks.cruise.tlb.twist;

import org.junit.Test;
import org.junit.After;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.file;
import static com.thoughtworks.cruise.tlb.utils.TestUtil.scenarios;
import com.thoughtworks.cruise.tlb.TlbFileResource;

public class LoadBalancedTwistSuiteTest {

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File("destination"));
    }

    @Test
    public void shouldLoadScenarioExecutorFromClassPath() throws Exception {
        TestSplitterCriteria criteria = mock(TestSplitterCriteria.class);

        File folder = folder("folder");
        when(criteria.filter(any(List.class))).thenReturn(scenarioResource(folder,1, 2));

        LoadBalancedTwistSuite suite = new LoadBalancedTwistSuite(criteria);

        suite.balance(folder.getAbsolutePath(), "destination");

        File destination = new File("destination");
        assertThat(destination.exists(), is(true));
        assertThat(destination.isDirectory(), is(true));
        assertThat(FileUtils.listFiles(destination, null, false).size(), is(2));
    }

    private File folder(String name) {
        File folder = new File(name);
        folder.mkdir();
        folder.deleteOnExit();
        return folder;
    }

    private List<TlbFileResource> scenarioResource(File folder, int... names) throws IOException {
        List<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (int name : names) {
            File file = new File(folder.getAbsolutePath(), "base" + name);
            file.createNewFile();
            file.deleteOnExit();
            resources.add(new SceanrioFileResource(file));
        }
        return resources;
    }
}
