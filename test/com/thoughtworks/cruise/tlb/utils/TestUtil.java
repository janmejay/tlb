package com.thoughtworks.cruise.tlb.utils;

import com.thoughtworks.cruise.tlb.ant.JunitFileResource;
import com.thoughtworks.cruise.tlb.TlbFileResource;
import com.thoughtworks.cruise.tlb.twist.SceanrioFileResource;
import org.apache.tools.ant.Project;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import com.thoughtworks.cruise.tlb.utils.SystemEnvironment;

public class TestUtil {
    public static List<TlbFileResource> files(int ... numbers) {
        ArrayList<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (int number : numbers) {
            resources.add(file("base" + number));
        }
        return resources;
    }

    public static TlbFileResource file(String name) {
        return new JunitFileResource(new File(name));
    }

    public static TlbFileResource file(String dir, String name) {
        JunitFileResource fileResource = new JunitFileResource(new Project(), dir + File.separator + name + ".class");
        fileResource.setBaseDir(new File("."));
        return fileResource;
    }

    public static List<TlbFileResource> scenarios(int ... numbers) {
        List<TlbFileResource> resources = new ArrayList<TlbFileResource>();
        for (int number : numbers) {
            resources.add(new SceanrioFileResource(new File("base" + number)));
        }
        return resources;
    }

    public static SystemEnvironment initEnvironment(String jobName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_JOB_NAME, jobName);
        map.put(com.thoughtworks.cruise.tlb.TlbConstants.CRUISE_STAGE_NAME, "stage-1");
        return new SystemEnvironment(map);
    }
}
