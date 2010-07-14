package com.github.tlb.server;

import com.github.tlb.TlbConstants;
import com.github.tlb.server.repo.EntryRepoFactory;
import com.github.tlb.utils.SystemEnvironment;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @understands running the server as a standalone process
 */
public class TlbServerInitializer extends ServerInitializer {
    private final SystemEnvironment env;
    private final Timer timer;

    public TlbServerInitializer(SystemEnvironment env) {
        this(env, new Timer());
    }

    public TlbServerInitializer(SystemEnvironment env, Timer timer) {
        this.env = env;
        this.timer = timer;
    }

    protected Restlet application() {
        HashMap<String, Object> appMap = new HashMap<String, Object>();
        final EntryRepoFactory repoFactory = repoFactory();

        final int versionLifeInDays = Integer.parseInt(env.getProperty(TlbConstants.Server.VERSION_LIFE_IN_DAYS, "1"));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repoFactory.purgeVersionsOlderThan(versionLifeInDays);
            }
        }, 0, 1*24*60*60*1000);

        repoFactory.registerExitHook();
        appMap.put(TlbConstants.Server.REPO_FACTORY, repoFactory);
        Context applicationContext = new Context();
        applicationContext.setAttributes(appMap);
        return new TlbApplication(applicationContext);
    }

    @Override
    protected int appPort()  {
        return Integer.parseInt(env.getProperty(TlbConstants.Server.TLB_PORT, "7019"));
    }

    EntryRepoFactory repoFactory() {
        return new EntryRepoFactory(env);
    }
}
