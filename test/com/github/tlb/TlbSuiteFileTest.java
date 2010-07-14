package com.github.tlb;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TlbSuiteFileTest {
    @Test
    public void shouldParseTlbSuiteFileList() {
        final List<TlbSuiteFile> tlbSuiteFiles = TlbSuiteFileImpl.parse("foo/bar/Baz.class\nbar/baz/quux.rb\nhello/world.py\n");
        assertThat(tlbSuiteFiles.size(), is(3));
        assertThat(tlbSuiteFiles.get(0), is((TlbSuiteFile) new TlbSuiteFileImpl("foo/bar/Baz.class")));
        assertThat(tlbSuiteFiles.get(1), is((TlbSuiteFile) new TlbSuiteFileImpl("bar/baz/quux.rb")));
        assertThat(tlbSuiteFiles.get(2), is((TlbSuiteFile) new TlbSuiteFileImpl("hello/world.py")));
    }
    
    @Test
    public void shouldIgnoreExtraSeperatorsParseTlbSuiteFileList() {
        final List<TlbSuiteFile> tlbSuiteFiles = TlbSuiteFileImpl.parse("\n\n\nfoo/bar/Baz.class\n\n\n\n\nbar/baz/quux.rb\nhello/world.py\n\n\n\n\n\n");
        assertThat(tlbSuiteFiles.size(), is(3));
        assertThat(tlbSuiteFiles.get(0), is((TlbSuiteFile) new TlbSuiteFileImpl("foo/bar/Baz.class")));
        assertThat(tlbSuiteFiles.get(1), is((TlbSuiteFile) new TlbSuiteFileImpl("bar/baz/quux.rb")));
        assertThat(tlbSuiteFiles.get(2), is((TlbSuiteFile) new TlbSuiteFileImpl("hello/world.py")));
    }
    
    @Test
    public void shouldDumpFileNameFollowedByNewline() {
        assertThat(new TlbSuiteFileImpl("foo/bar/Baz.class").dump(), is("foo/bar/Baz.class\n"));
    }
}
