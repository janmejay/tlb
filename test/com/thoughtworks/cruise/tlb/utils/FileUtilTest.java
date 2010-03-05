package com.thoughtworks.cruise.tlb.utils;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.io.File;

import com.thoughtworks.cruise.tlb.TlbConstants;

public class FileUtilTest {
    private FileUtil fileUtil;

    @Before
    public void setUp() throws Exception {
        HashMap<String, String> envMap = new HashMap<String, String>();
        envMap.put(TlbConstants.TLB_TMP_DIR, "/foo/bar");
        fileUtil = new FileUtil(new SystemEnvironment(envMap));
    }

    @Test
    public void testClassFileRelativePath() {
        assertThat(fileUtil.classFileRelativePath("com.thoughtworks.cruise.Foo"), is("com/thoughtworks/cruise/Foo.class"));
    }

    @Test
    public void testGetsUniqueFileForGivenStringUnderTmpDir() {
        File uniqueFile = fileUtil.getUniqueFile("foo_bar_baz");
        assertThat(uniqueFile.getParentFile().getAbsolutePath(), is("/foo/bar"));
    }

    @Test
    public void shouldDefaultTmpDirToSystemTmpDir() throws Exception{
        FileUtil util = new FileUtil(new SystemEnvironment(new HashMap<String, String>()));
        assertThat(fileUtil.tempFolder(), is("/foo/bar"));
        assertThat(util.tempFolder(), is(System.getProperty("java.io.tmpdir")));
    }
}
