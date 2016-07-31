package org.osgl.inject;

import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

public class CDIAdaptor implements GeniePlugin {
    @Override
    public void register(Genie genie) {
        genie.registerScopeAlias(Singleton.class, ApplicationScoped.class);
        genie.registerScopeAlias(SessionScoped.class, javax.enterprise.context.SessionScoped.class);
        genie.registerScopeAlias(RequestScoped.class, javax.enterprise.context.RequestScoped.class);
    }
}
