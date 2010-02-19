package com.thoughtworks.cruise.tlb.utils;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import org.hamcrest.Matcher;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

import com.thoughtworks.cruise.tlb.TlbConstants;

/**
 * @understands
 */
public class FileUtilTest {
    @Test
    public void testClassFileRelativePath() {
        assertThat(FileUtil.classFileRelativePath("com.thoughtworks.cruise.Foo"), is("com/thoughtworks/cruise/Foo.class"));
    }

    @Test
    public void testGetsUniqueFileForGivenStringUnderTmpDir() {
        File uniqueFile = FileUtil.getUniqueFile("foo_bar_baz");
        assertThat(uniqueFile.getParentFile().getAbsolutePath(), is(FileUtil.tempFolder()));
    }
}
