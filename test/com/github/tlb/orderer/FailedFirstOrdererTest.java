package com.github.tlb.orderer;

import com.github.tlb.TestUtil;

import static com.github.tlb.TestUtil.initEnvironment;

import com.github.tlb.TlbFileResource;
import com.github.tlb.TlbSuiteFile;
import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.splitter.TalksToService;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import com.github.tlb.utils.SuiteFileConvertor;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import org.apache.tools.ant.Project;
import org.mockito.internal.verification.Times;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class FailedFirstOrdererTest {
    private FailedFirstOrderer orderer;
    private TalkToCruise toCruise;
    private Project project;
    private String baseDir;

    @Before
    public void setUp() throws Exception {
        orderer = new FailedFirstOrderer(initEnvironment("job-1"));
        toCruise = mock(TalkToCruise.class);
        project = new Project();
        baseDir = TestUtil.createTempFolder().getAbsolutePath();
        project.setBasedir(baseDir);
        orderer.talksToService(toCruise);
    }

    @Test
    public void shouldImplementTalksToCruise() throws Exception{
        assertTrue("Failed first orderer must be talk to cruise aware", TalksToService.class.isAssignableFrom(FailedFirstOrderer.class));
    }

    @Test
    public void shouldNotReorderTestsWhenNoneFailed() throws Exception{
        JunitFileResource bazClass = junitFileResource(baseDir, "foo/bar/Baz.class");
        JunitFileResource quuxClass = junitFileResource(baseDir, "foo/baz/Quux.class");
        JunitFileResource bangClass = junitFileResource(baseDir, "foo/baz/Bang.class");
        List<SuiteResultEntry> failedTests = Arrays.asList(new SuiteResultEntry("baz/bang/Foo.class", true), new SuiteResultEntry("foo/bar/Bang.class", true));
        when(toCruise.getLastRunFailedTests()).thenReturn(failedTests);
        List<TlbFileResource> fileList = new ArrayList<TlbFileResource>(Arrays.asList(bazClass, quuxClass, bangClass));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> tlbSuiteFiles = convertor.toTlbSuiteFiles(fileList);
        Collections.sort(tlbSuiteFiles, orderer);
        final List<TlbFileResource> resources = new ArrayList<TlbFileResource>(Arrays.asList(bazClass, quuxClass, bangClass));
        assertThat(convertor.toTlbFileResources(tlbSuiteFiles), is(resources));
        verify(toCruise, new Times(1)).getLastRunFailedTests();
    }

    @Test
    public void shouldReorderTestsToBringFailedTestsFirst() throws Exception{
        JunitFileResource bazClass = junitFileResource(baseDir, "foo/bar/Baz.class");
        JunitFileResource quuxClass = junitFileResource(baseDir, "foo/baz/Quux.class");
        JunitFileResource failedFooClass = junitFileResource(baseDir, "baz/bang/Foo.class");
        JunitFileResource failedBangClass = junitFileResource(baseDir, "foo/bar/Bang.class");
        List<SuiteResultEntry> failedTests = Arrays.asList(new SuiteResultEntry("baz/bang/Foo.class", true), new SuiteResultEntry("foo/bar/Bang.class", true));
        when(toCruise.getLastRunFailedTests()).thenReturn(failedTests);
        List<TlbFileResource> fileList = new ArrayList<TlbFileResource>(Arrays.asList(bazClass, failedFooClass, quuxClass, failedBangClass));
        final SuiteFileConvertor convertor = new SuiteFileConvertor();
        final List<TlbSuiteFile> tlbSuiteFiles = convertor.toTlbSuiteFiles(fileList);
        Collections.sort(tlbSuiteFiles, orderer);
        fileList = convertor.toTlbFileResources(tlbSuiteFiles);

        assertThat(fileList.get(0), anyOf(is((TlbFileResource) failedBangClass), is((TlbFileResource) failedFooClass)));
        assertThat(fileList.get(1), anyOf(is((TlbFileResource) failedBangClass), is((TlbFileResource) failedFooClass)));

        assertThat(fileList.get(2), anyOf(is((TlbFileResource) bazClass), is((TlbFileResource) quuxClass)));
        assertThat(fileList.get(3), anyOf(is((TlbFileResource) bazClass), is((TlbFileResource) quuxClass)));
        verify(toCruise, new Times(1)).getLastRunFailedTests();
    }

    private JunitFileResource junitFileResource(String baseDir, String classRelPath) {
        JunitFileResource bazClass = new JunitFileResource(project, classRelPath);
        bazClass.setBaseDir(new File(baseDir));
        return bazClass;
    }
}
