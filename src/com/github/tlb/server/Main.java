package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.utils.SystemEnvironment;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

import java.io.File;
import java.util.HashMap;

/**
 * @understands running the server as a standalone process
 */
public class Main {
    public static final String DATA = "data";
    private final SystemEnvironment env;
    public static final String TLB_STORE_DIR = "tlb_store";

    Component init() {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, Integer.parseInt(env.getProperty(TlbConstants.TLB_PORT, "7019")));
        component.getDefaultHost().attach(new TlbApplication(appContext()));
        return component;
    }

    Context appContext() {
        HashMap<String, Object> appMap = new HashMap<String, Object>();
        EntryRepoFactory repoFactory = repoFactory();
        repoFactory.registerExitHook();
        appMap.put(TlbConstants.Server.REPO_FACTORY, repoFactory);
        Context applicationContext = new Context();
        applicationContext.setAttributes(appMap);
        return applicationContext;
    }

    EntryRepoFactory repoFactory() {
        File storeDir = new File(TLB_STORE_DIR);
        return new EntryRepoFactory(storeDir);
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
