package org.osgl.inject;

import org.osgl.inject.ScopedObjects.SessionProduct;
import org.osgl.inject.annotation.Provides;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

class CDIScopedFactory extends Module {

    @Override
    protected void configure() {
        bind(ScopeCache.SessionScope.class).to(ScopedFactory.SESSION_SCOPE_PROVIDER);
    }

    @Provides
    @SessionScoped
    static SessionProduct createSession(ScopedObjects.JEESessionObject bean) {
        return bean;
    }

    static class ProductHolder {
        @SessionScoped
        @Inject
        SessionProduct product;
    }
}
