package org.osgl.genie;

import org.osgl.genie.annotation.Provides;
import org.osgl.genie.spi.ScopeResolver;

import javax.enterprise.context.NormalScope;
import javax.inject.Inject;
import javax.inject.Scope;
import java.lang.annotation.Annotation;

public class JEEScopeResolver implements ScopeResolver {
    @Override
    public boolean isScope(Class<? extends Annotation> annoClass) {
        return annoClass.isAnnotationPresent(Scope.class) || annoClass.isAnnotationPresent(NormalScope.class);
    }

    @Provides
    public ScopeResolver scopeResolver() {
        return this;
    }

    @Provides
    public ScopedObjects.RequestProduct requestProduct(ScopedObjects.JEERequestObject prod) {
        return prod;
    }

    static class RequestProdHolder {
        ScopedObjects.RequestProduct prod;
        @Inject
        RequestProdHolder(ScopedObjects.RequestProduct prod) {
            this.prod = prod;
        }
    }
}
