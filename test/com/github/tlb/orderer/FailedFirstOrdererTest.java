package com.github.tlb.orderer;

import com.github.tlb.TestUtil;

import static com.github.tlb.TestUtil.initEnvironment;
import com.github.tlb.ant.JunitFileResource;
import com.github.tlb.domain.SuiteResultEntry;
import com.github.tlb.service.TalkToCruise;
import com.github.tlb.splitter.TalksToCruise;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import org.apache.tools.ant.Project;
import org.mockito.internal.verification.Times;
import org.mockito.internal.verification.api.VerificationMode;

import java.io.File;
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
        orderer.talksToCruise(toCruise);
    }

    @Test
    public void shouldImplementTalksToCruise() throws Exception{
        assertTrue("Failed first orderer must be talk to cruise aware", TalksToCruise.class.isAssignableFrom(FailedFirstOrderer.class));
    }

    @Test
    public void shouldNotReorderTestsWhenNoneFailed() throws Exception{
        JunitFileResource bazClass = junitFileResource(baseDir, "foo/bar/Baz.class");
        JunitFileResource quuxClass = junitFileResource(baseDir, "foo/baz/Quux.class");
        JunitFileResource bangClass = junitFileResource(baseDir, "foo/baz/Bang.class");
        List<SuiteResultEntry> failedTests = Arrays.asList(new SuiteResultEntry("baz.bang.Foo.class", true), new SuiteResultEntry("foo.bar.Bang.class", true));
        when(toCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));
        when(toCruise.getLastRunFailedTests(Arrays.asList("job-1", "job-2", "job-3"))).thenReturn(failedTests);
        List<JunitFileResource> fileList = Arrays.asList(bazClass, quuxClass, bangClass);
        Collections.sort(fileList, orderer);
        assertThat(fileList, is(Arrays.asList(bazClass, quuxClass, bangClass)));
        verify(toCruise, new Times(1)).pearJobs();
        verify(toCruise, new Times(1)).getLastRunFailedTests(Arrays.asList("job-1", "job-2", "job-3"));
    }

    @Test
    public void shouldReorderTestsToBringFailedTestsFirst() throws Exception{
        JunitFileResource bazClass = junitFileResource(baseDir, "foo/bar/Baz.class");
        JunitFileResource quuxClass = junitFileResource(baseDir, "foo/baz/Quux.class");
        JunitFileResource failedFooClass = junitFileResource(baseDir, "baz/bang/Foo.class");
        JunitFileResource failedBangClass = junitFileResource(baseDir, "foo/bar/Bang.class");
        List<SuiteResultEntry> failedTests = Arrays.asList(new SuiteResultEntry("baz.bang.Foo", true), new SuiteResultEntry("foo.bar.Bang", true));
        when(toCruise.pearJobs()).thenReturn(Arrays.asList("job-1", "job-2", "job-3"));
        when(toCruise.getLastRunFailedTests(Arrays.asList("job-1", "job-2", "job-3"))).thenReturn(failedTests);
        List<JunitFileResource> fileList = Arrays.asList(bazClass, failedFooClass, quuxClass, failedBangClass);
        Collections.sort(fileList, orderer);

        assertThat(fileList.get(0), anyOf(is(failedBangClass), is(failedFooClass)));
        assertThat(fileList.get(1), anyOf(is(failedBangClass), is(failedFooClass)));

        assertThat(fileList.get(2), anyOf(is(bazClass), is(quuxClass)));
        assertThat(fileList.get(3), anyOf(is(bazClass), is(quuxClass)));
        verify(toCruise, new Times(1)).pearJobs();
        verify(toCruise, new Times(1)).getLastRunFailedTests(Arrays.asList("job-1", "job-2", "job-3"));
    }

    private JunitFileResource junitFileResource(String baseDir, String classRelPath) {
        JunitFileResource bazClass = new JunitFileResource(project, classRelPath);
        bazClass.setBaseDir(new File(baseDir));
        return bazClass;
    }
}
