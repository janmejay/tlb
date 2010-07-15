package com.github.tlb;

import com.github.tlb.factory.TlbFactory;
import com.github.tlb.server.ServerInitializer;
import com.github.tlb.utils.SystemEnvironment;

/**
 * @understands launching a restlet server
 */
public class Main {
    public static void main(String[] args) {
        final Main main = new Main();
        try {
            main.restletInitializer(new SystemEnvironment()).init().start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ServerInitializer restletInitializer(SystemEnvironment environment) {
        return TlbFactory.getRestletLauncher(environment.getProperty(TlbConstants.TLB_APP), environment);
    }
}
