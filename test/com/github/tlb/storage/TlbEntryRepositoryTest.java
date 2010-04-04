package com.github.tlb.storage;

import com.github.tlb.TestUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TlbEntryRepositoryTest {
    private TestUtil.LogFixture logFixture;

    @Before
    public void setUp() {
        logFixture = new TestUtil.LogFixture();
    }

    @Test
    public void shouldLogWhenLoadingOrPersistingCachableData() throws Exception{
        File tmpDir = TestUtil.createTempFolder();

        File file = new File(tmpDir, "foo");
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            //ignore, file may not be there!
        }

        TlbEntryRepository cruise = new TlbEntryRepository(tmpDir.getAbsolutePath(), "foo");

        logFixture.startListening();
        cruise.appendLine("hello world\n");
        logFixture.assertHeard(String.format("Wrote [ hello world\n ] to %s", file.getAbsolutePath()));
        cruise.appendLine("hacking is fun\n");
        logFixture.assertHeard(String.format("Wrote [ hacking is fun\n ] to %s", file.getAbsolutePath()));
        cruise.appendLine("foo bar baz quux\n");
        logFixture.assertHeard(String.format("Wrote [ foo bar baz quux\n ] to %s", file.getAbsolutePath()));
        cruise.load();
        logFixture.assertHeard(String.format("Cached 3 lines from %s, the last of which was [ foo bar baz quux ]", file.getAbsolutePath()));
    }
}
