package com.thoughtworks.cruise.tlb;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.Project;

import java.util.*;
import java.io.File;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;
import com.thoughtworks.cruise.tlb.utils.FileUtil;

public class TestUtil {
    public static List<FileResource> files(int ... numbers) {
        ArrayList<FileResource> resources = new ArrayList<FileResource>();
        for (int number : numbers) {
            resources.add(file("base" + number));
        }
        return resources;
    }

    public static FileResource file(String name) {
        return new FileResource(new File(name));
    }

    public static FileResource file(String dir, String name) {
        FileResource fileResource = new FileResource(new Project(), dir + File.separator + name + ".class");
        fileResource.setBaseDir(new File("."));
        return fileResource;
    }

    public static SystemEnvironment initEnvironment(String jobName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_JOB_NAME, jobName);
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }

    public static File createTempFolder() {
        final File file = new File(System.getProperty(FileUtil.TMP_DIR), UUID.randomUUID().toString());
        file.mkdirs();
        file.deleteOnExit();
        return file;
    }
}
