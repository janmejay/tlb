package com.github.tlb.server;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * @understands initializing a restlet application
 */
public abstract class ServerInitializer {
    public final Component init() {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, appPort());
        component.getDefaultHost().attach(application());
        return component;
    }

    protected abstract Restlet application();

    protected abstract int appPort();
}
