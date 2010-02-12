package com.thoughtworks.cruise.tlb;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.Project;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

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
        return new FileResource(new Project(), new File(dir, name).getPath());
    }

    public static SystemEnvironment initEnvironment(String jobName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_JOB_NAME, jobName);
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }
}
