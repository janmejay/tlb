package com.thoughtworks.cruise.tlb.orderer;

import com.thoughtworks.cruise.tlb.TestUtil;
import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.service.TalkToCruise;
import com.thoughtworks.cruise.tlb.splitter.TalksToCruise;
import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import org.apache.tools.ant.Project;

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
        orderer = new FailedFirstOrderer(new SystemEnvironment());
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
        List<String> failedTests = Arrays.asList("baz.bang.Foo.class", "foo.bar.Bang.class");
        when(toCruise.failedTests()).thenReturn(failedTests);
        List<JunitFileResource> fileList = Arrays.asList(bazClass, quuxClass, bangClass);
        Collections.sort(fileList, orderer);
        assertThat(fileList, is(Arrays.asList(bazClass, quuxClass, bangClass)));
    }

    @Test
    public void shouldReorderTestsToBringFailedTestsFirst() throws Exception{
        JunitFileResource bazClass = junitFileResource(baseDir, "foo/bar/Baz.class");
        JunitFileResource quuxClass = junitFileResource(baseDir, "foo/baz/Quux.class");
        JunitFileResource failedFooClass = junitFileResource(baseDir, "baz/bang/Foo.class");
        JunitFileResource failedBangClass = junitFileResource(baseDir, "foo/bar/Bang.class");
        List<String> failedTests = Arrays.asList("baz.bang.Foo", "foo.bar.Bang");
        when(toCruise.failedTests()).thenReturn(failedTests);
        List<JunitFileResource> fileList = Arrays.asList(bazClass, failedFooClass, quuxClass, failedBangClass);
        Collections.sort(fileList, orderer);

        assertThat(fileList.get(0), anyOf(is(failedBangClass), is(failedFooClass)));
        assertThat(fileList.get(1), anyOf(is(failedBangClass), is(failedFooClass)));

        assertThat(fileList.get(2), anyOf(is(bazClass), is(quuxClass)));
        assertThat(fileList.get(3), anyOf(is(bazClass), is(quuxClass)));
    }

    private JunitFileResource junitFileResource(String baseDir, String classRelPath) {
        JunitFileResource bazClass = new JunitFileResource(project, classRelPath);
        bazClass.setBaseDir(new File(baseDir));
        return bazClass;
    }
}
