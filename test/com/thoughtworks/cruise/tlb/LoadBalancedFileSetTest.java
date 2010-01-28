package com.thoughtworks.cruise.tlb;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.FileSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Properties;
import java.io.File;

import com.thoughtworks.cruise.tlb.utils.FileUtil;
import com.thoughtworks.cruise.tlb.splitter.TestSplitterCriteria;

public class LoadBalancedFileSetTest {
    private LoadBalancedFileSet fileSet;
    private File projectDir;
    private Properties originalProperties;

    @Before
    public void setUp() throws Exception {
        fileSet = new LoadBalancedFileSet();
        projectDir = FileUtil.createTempFolder();
        initFileSet(fileSet);
        originalProperties = System.getProperties();
    }

    private void initFileSet(FileSet fileSet) {
        fileSet.setDir(projectDir);
        fileSet.setProject(new Project());
    }

    @After
    public void tearDown() {
        System.setProperties(originalProperties);
    }

    @Test
    public void shouldReturnAllFilesWhenThereIsNothingToSplit() {
        File newFile = FileUtil.createFileInFolder(projectDir, "abc");

        Iterator files = fileSet.iterator();

        assertThat(files.hasNext(), is(true));
        assertThat(((FileResource) files.next()).getFile(), is (newFile));
        assertThat(files.hasNext(), is(false));
    }

    @Test
    public void shouldReturnAListOfFilesWhichMatchAGivenMatcher() {
        File excluded = FileUtil.createFileInFolder(projectDir, "excluded");
        File included = FileUtil.createFileInFolder(projectDir, "included");


        TestSplitterCriteria criteria = mock(TestSplitterCriteria.class);
        when(criteria.shouldInclude(included)).thenReturn(true);
        when(criteria.shouldInclude(excluded)).thenReturn(false);

        fileSet = new LoadBalancedFileSet(criteria);
        initFileSet(fileSet);
        Iterator files = fileSet.iterator();

        assertThat(files.hasNext(), is(true));
        assertThat(((FileResource) files.next()).getFile(), is (included));
        assertThat(files.hasNext(), is(false));
    }
}
