package com.github.tlb.utils;

import com.github.tlb.TestUtil;
import com.github.tlb.TlbConstants;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.core.Is.is;
import org.apache.commons.io.FileUtils;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import com.googlecode.junit.ext.RunIf;
import com.googlecode.junit.ext.checkers.OSChecker;

public class FileUtilTest {
    private FileUtil fileUtil;
    private TestUtil.LogFixture logFixture;
    private String javaTmpDir;
    private String overriddenTmpDir;

    @Before
    public void setUp() throws Exception {
        javaTmpDir = System.getProperty("java.io.tmpdir");
        overriddenTmpDir = javaTmpDir + "/tlb_dir/foo/bar";
        logFixture = new TestUtil.LogFixture();

        HashMap<String, String> envMap = new HashMap<String, String>();
        deleteOverriddenTmpDirIfExists();

        envMap.put(TlbConstants.TLB_TMP_DIR, overriddenTmpDir);
        fileUtil = new FileUtil(new SystemEnvironment(envMap));
    }

    private void deleteOverriddenTmpDirIfExists() throws IOException {
        File tmpDir = new File(overriddenTmpDir);
        if (tmpDir.exists()) {
            if (tmpDir.isDirectory()) {
                FileUtils.deleteDirectory(tmpDir);
            } else {
                FileUtils.forceDelete(tmpDir);
            }
        }
    }

    @After
    public void tearDown() throws IOException {
        logFixture.stopListening();
        deleteOverriddenTmpDirIfExists();
    }

    @Test
    public void testClassFileRelativePath() {
        assertThat(fileUtil.classFileRelativePath("com.thoughtworks.cruise.Foo"), is("com/thoughtworks/cruise/Foo.class"));
    }

    @Test
    public void testGetsUniqueFileForGivenStringUnderTmpDir() {
        logFixture.startListening();
        File uniqueFile = fileUtil.getUniqueFile("foo_bar_baz");
        assertThat(uniqueFile.getParentFile().getAbsolutePath(), is(overriddenTmpDir));
        logFixture.assertHeard(String.format("unique file name foo_bar_baz translated to %s", uniqueFile.getAbsolutePath()));
    }

    @Test
    public void shouldDefaultTmpDirToSystemTmpDir() throws Exception{
        logFixture.startListening();
        assertThat(fileUtil.tmpDir(), is(overriddenTmpDir));
        logFixture.assertNotHeard("defaulting");
        logFixture.assertHeard(String.format("using %s as tlb temp directory", overriddenTmpDir));
        logFixture.assertHeard(String.format("checking for existance of directory %s as tlb tmpdir", overriddenTmpDir));
        logFixture.assertHeard(String.format("directory %s doesn't exist, creating it now", overriddenTmpDir));
        assertThat(new File(overriddenTmpDir).exists(), is(true));

        FileUtil util = new FileUtil(new SystemEnvironment(new HashMap<String, String>()));
        assertThat(util.tmpDir(), is(javaTmpDir));
        logFixture.assertHeard(String.format("defaulting tlb tmp directory to %s", javaTmpDir));
        logFixture.assertHeard(String.format("using %s as tlb temp directory", javaTmpDir));
        logFixture.assertHeard(String.format("checking for existance of directory %s as tlb tmpdir", javaTmpDir));
        logFixture.assertHeard(String.format("directory %s exists, creation not required", javaTmpDir));
    }

    @Test
    public void shouldFailIfFindsAFileInPlaceOfDirectoryForTmpDir() throws Exception{
        FileUtils.writeStringToFile(new File(overriddenTmpDir), "hello world");
        logFixture.startListening();
        try {
            fileUtil.tmpDir();
            fail("should have failed as a file exists in place of tmp dir");
        } catch (Exception e) {
            //ignore
        }
        logFixture.assertHeard(String.format("tlb tmp dir %s is a file, it must be a directory", overriddenTmpDir));
    }

    @Test
    public void shouldMakeTmpDirIfNonExistant() throws Exception{
        assertThat(new File(overriddenTmpDir).exists(), is(false));
        logFixture.startListening();
        fileUtil.tmpDir();
        logFixture.assertHeard(String.format("using %s as tlb temp directory", overriddenTmpDir));
        logFixture.assertHeard(String.format("checking for existance of directory %s as tlb tmpdir", overriddenTmpDir));
        logFixture.assertHeard(String.format("directory %s doesn't exist, creating it now", overriddenTmpDir));
        logFixture.assertHeard(String.format("created directory %s, which is to be used as tlb tmp dir.", overriddenTmpDir));
        File tmpDir = new File(overriddenTmpDir);
        assertThat(tmpDir.exists(), is(true));
        assertThat(tmpDir.isDirectory(), is(true));
    }

    @Test
    @RunIf(value = OSChecker.class, arguments = OSChecker.LINUX)
    public void shouldFailIfDirCouldNotBeCreated() throws Exception{
        HashMap<String, String> envMap = new HashMap<String, String>();
        String tlbTmpDirPath = "/var/lib/tlb_data";
        envMap.put(TlbConstants.TLB_TMP_DIR, tlbTmpDirPath);
        FileUtil util = new FileUtil(new SystemEnvironment(envMap));
        assertThat(new File(tlbTmpDirPath).exists(), is(false));
        logFixture.startListening();
        try {
            util.tmpDir();
            fail("should have bombed on failing to create tmp dir");
        } catch (Exception e) {
            //ignore
        }
        logFixture.assertHeard(String.format("using %s as tlb temp directory", tlbTmpDirPath));
        logFixture.assertHeard(String.format("checking for existance of directory %s as tlb tmpdir", tlbTmpDirPath));
        logFixture.assertHeard(String.format("directory %s doesn't exist, creating it now", tlbTmpDirPath));
        logFixture.assertHeard(String.format("could not create directory %s", tlbTmpDirPath));
        File tmpDir = new File(tlbTmpDirPath);
        assertThat(tmpDir.exists(), is(false));
    }
}
