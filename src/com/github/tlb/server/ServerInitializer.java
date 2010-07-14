package com.github.tlb.server;

import org.restlet.Component;

/**
 * @understands initializing a restlet application
 */
public interface ServerInitializer {
    Component init();
}
