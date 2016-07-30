package org.osgl.inject;

import org.osgl.inject.ScopedObjects.SessionProduct;
import org.osgl.inject.annotation.Provides;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

class CDIScopedFactory {

    @Provides
    @SessionScoped
    public static SessionProduct createSession(ScopedObjects.JEESessionObject bean) {
        return bean;
    }

    static class ProductHolder {
        @SessionScoped
        @Inject
        SessionProduct product;
    }
}
