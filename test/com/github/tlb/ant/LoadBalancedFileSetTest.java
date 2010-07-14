package com.github.tlb.ant;

import static com.github.tlb.TlbConstants.Cruise.CRUISE_SERVER_URL;
import static com.github.tlb.TlbConstants.TLB_CRITERIA;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.splitter.CountBasedTestSplitterCriteria;
import com.github.tlb.splitter.JobFamilyAwareSplitterCriteria;
import com.github.tlb.utils.FileUtil;
import com.github.tlb.utils.SystemEnvironment;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import com.github.tlb.orderer.TestOrderer;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.*;

public class LoadBalancedFileSetTest {
    private LoadBalancedFileSet fileSet;
    private File projectDir;
    private FileUtil fileUtil;

    @Before
    public void setUp() throws Exception {
        SystemEnvironment env = new SystemEnvironment(new HashMap<String, String>());
        fileSet = new LoadBalancedFileSet(env);
        fileUtil = new FileUtil(env);
        projectDir = TestUtil.createTempFolder();
        initFileSet(fileSet);
    }

    private void initFileSet(FileSet fileSet) {
        fileSet.setDir(projectDir);
        fileSet.setProject(new Project());
    }

    @Test
    public void shouldReturnAllFilesWhenThereIsNothingToSplit() {
        File newFile = TestUtil.createFileInFolder(projectDir, "abc");

        Iterator files = fileSet.iterator();

        assertThat(files.hasNext(), is(true));
        assertThat(((FileResource) files.next()).getFile(), is(newFile));
        assertThat(files.hasNext(), is(false));
    }

    @Test
    public void shouldReturnAListOfFilesWhichMatchAGivenMatcher() {
        TestUtil.createFileInFolder(projectDir, "excluded");
        File included = TestUtil.createFileInFolder(projectDir, "included");

        JobFamilyAwareSplitterCriteria criteria = mock(JobFamilyAwareSplitterCriteria.class);
        TlbFileResource fileResource = new JunitFileResource(included);
        when(criteria.filter(any(List.class))).thenReturn(Arrays.asList(fileResource));

        fileSet = new LoadBalancedFileSet(criteria, TestOrderer.NO_OP);
        fileSet.setDir(projectDir);
        initFileSet(fileSet);
        Iterator files = fileSet.iterator();

        assertThat(files.hasNext(), is(true));
        assertThat(((FileResource) files.next()).getFile(), is(included));
        assertThat(files.hasNext(), is(false));
    }

    @Test
    public void shouldReturnAListOfFilesInOrderReturnedByReorderer() {
        TestUtil.createFileInFolder(projectDir, "excluded");
        JunitFileResource resourceOne = new JunitFileResource(TestUtil.createFileInFolder(projectDir, "C"));
        JunitFileResource resourceTwo = new JunitFileResource(TestUtil.createFileInFolder(projectDir, "B"));
        JunitFileResource resourceThree = new JunitFileResource(TestUtil.createFileInFolder(projectDir, "A"));

        JobFamilyAwareSplitterCriteria criteria = mock(JobFamilyAwareSplitterCriteria.class);

        when(criteria.filter(any(List.class))).thenReturn(Arrays.asList((TlbFileResource) resourceOne, resourceTwo, resourceThree));

        fileSet = new LoadBalancedFileSet(criteria, new TestOrderer(new SystemEnvironment()) {
            public int compare(TlbSuiteFile o1, TlbSuiteFile o2) {
                return 1;
            }
        });
        fileSet.setDir(projectDir);
        initFileSet(fileSet);
        Iterator files = fileSet.iterator();

        assertThat(files.hasNext(), is(true));
        List<FileResource> resources = new ArrayList<FileResource>();

        while (files.hasNext()) {
            resources.add((FileResource) files.next());
        }
        List<FileResource> expectedResourcesOrder = new ArrayList<FileResource>();
        expectedResourcesOrder.add(resourceThree.getFileResource());
        expectedResourcesOrder.add(resourceTwo.getFileResource());
        expectedResourcesOrder.add(resourceOne.getFileResource());
        assertThat(resources, is(expectedResourcesOrder));
    }

    @Test
    public void shouldUseSystemPropertyToInstantiateCriteria() {
        fileSet = new LoadBalancedFileSet(initEnvironment("com.github.tlb.splitter.CountBasedTestSplitterCriteria"));
        fileSet.setDir(projectDir);
        assertThat(fileSet.getSplitterCriteria(), instanceOf(CountBasedTestSplitterCriteria.class));
    }

    @Test
    public void shouldSetFileSetDir() throws Exception{
        JobFamilyAwareSplitterCriteria criteria = mock(JobFamilyAwareSplitterCriteria.class);
        fileSet = new LoadBalancedFileSet(criteria, TestOrderer.NO_OP);
        fileSet.setDir(projectDir);
        verify(criteria).setDir(projectDir);
        assertThat(fileSet.getDir(), is(projectDir));
    }

    private SystemEnvironment initEnvironment(String strategyName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TLB_CRITERIA, strategyName);
        map.put(CRUISE_SERVER_URL, "https://localhost:8154/cruise");
        return new SystemEnvironment(map);
    }
}
