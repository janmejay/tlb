package com.thoughtworks.cruise.tlb.utils;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @understands
 */
public class FileUtilTest {
    @Test
    public void testClassFileRelativePath() {
        assertThat(FileUtil.classFileRelativePath("com.thoughtworks.cruise.Foo"), is("com/thoughtworks/cruise/Foo.class"));
    }
}
