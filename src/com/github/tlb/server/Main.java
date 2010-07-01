package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.utils.SystemEnvironment;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

import java.util.HashMap;

/**
 * @understands running the server as a standalone process
 */
public class Main {
    public static final String DATA = "data";
    private final SystemEnvironment env;

    Component init() {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, Integer.parseInt(env.getProperty(TlbConstants.TLB_PORT, "7019")));
        component.getDefaultHost().attach(new TlbApplication(appContext()));
        return component;
    }

    Context appContext() {
        HashMap<String, Object> appMap = new HashMap<String, Object>();
        appMap.put(TlbConstants.Server.REPO_FACTORY, new EntryRepoFactory());
        Context applicationContext = new Context();
        applicationContext.setAttributes(appMap);
        return applicationContext;
    }

    Main(SystemEnvironment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        try {
            new Main(new SystemEnvironment()).init().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
