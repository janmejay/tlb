package com.github.tlb.utils;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.TlbSuiteFileImpl;
import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.twist.SceanrioFileResource;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class SuiteFileConvertorTest {
    protected SuiteFileConvertor convertor;
    protected ArrayList<TlbFileResource> resources;
    protected JunitFileResource bazTestResource;
    protected JunitFileResource fileTestResource;
    protected SceanrioFileResource fooScn;
    protected SceanrioFileResource barScn;
    protected SceanrioFileResource bazScn;
    protected JunitFileResource fooTest;

    @Before
    public void setUp() {
        convertor = new SuiteFileConvertor();
        resources = new ArrayList<TlbFileResource>();
        fooScn = new SceanrioFileResource(new File("foo.scn"));
        resources.add(fooScn);
        barScn = new SceanrioFileResource(new File("bar.scn"));
        resources.add(barScn);
        bazScn = new SceanrioFileResource(new File("baz.scn"));
        resources.add(bazScn);
        fooTest = new JunitFileResource(new File("FooTest.class"));
        resources.add(fooTest);
        final Project project = new Project();
        bazTestResource = new JunitFileResource(new FileResource(project, "BazTest.class"));
        resources.add(bazTestResource);
        fileTestResource = new JunitFileResource(new FileResource(new File("dir"), "FileTest.class"));
        resources.add(fileTestResource);
    }

    @Test
    public void shouldConvertAListOfTlbFileResourceToTlbSuiteFileInOrder() {
        List<TlbSuiteFile> suiteFiles = convertor.toTlbSuiteFiles(resources);
        assertThat(suiteFiles.size(), is(6));
        assertThat(suiteFiles.get(0).getName(), is(fooScn.getName()));
        assertThat(suiteFiles.get(1).getName(), is(barScn.getName()));
        assertThat(suiteFiles.get(2).getName(), is(bazScn.getName()));
        assertThat(suiteFiles.get(3).getName(), is(fooTest.getName()));
        assertThat(suiteFiles.get(4).getName(), is(bazTestResource.getName()));
        assertThat(suiteFiles.get(5).getName(), is(fileTestResource.getName()));
    }
    
    @Test
    public void shouldNotAllowConvertingToTlbSuiteFilesTwice() {
        convertor.toTlbSuiteFiles(resources);
        try {
            convertor.toTlbSuiteFiles(resources);
            fail("should not have allowed overwriting");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("overwriting of suite resource list is not allowed, new instance should be used"));
        }
    }

    @Test
    public void shouldFetchCorrespondingFileResourcesForSubsetOfTestSuiteFilesInOrder() {
        convertor.toTlbSuiteFiles(resources);
        final List<TlbSuiteFile> files = new ArrayList<TlbSuiteFile>();
        files.add(new TlbSuiteFileImpl(bazScn.getName()));
        files.add(new TlbSuiteFileImpl(fileTestResource.getName()));
        files.add(new TlbSuiteFileImpl(fooTest.getName()));
        files.add(new TlbSuiteFileImpl(fooScn.getName()));
        List<TlbFileResource> filteredSet = convertor.toTlbFileResources(files);
        assertThat(filteredSet.size(), is(4));
        assertThat(filteredSet.get(0), sameInstance((TlbFileResource) bazScn));
        assertThat(filteredSet.get(1), sameInstance((TlbFileResource) fileTestResource));
        assertThat(filteredSet.get(2), sameInstance((TlbFileResource) fooTest));
        assertThat(filteredSet.get(3), sameInstance((TlbFileResource) fooScn));
    }
}
