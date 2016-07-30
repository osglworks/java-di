package org.osgl.inject;

import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;

import javax.inject.Singleton;

class GuiceAdaptor implements GeniePlugin {
    @Override
    public void register(Genie genie) {
        genie.registerScopeAlias(Singleton.class, com.google.inject.Singleton.class);
        genie.registerScopeAlias(SessionScoped.class, com.google.inject.servlet.SessionScoped.class);
        genie.registerScopeAlias(RequestScoped.class, com.google.inject.servlet.RequestScoped.class);
        // TODO support Guice Module
    }
}
