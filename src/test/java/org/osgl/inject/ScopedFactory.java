package org.osgl.inject;

import org.osgl.inject.ScopedObjects.*;
import org.osgl.inject.annotation.Provides;
import org.osgl.inject.annotation.SessionScoped;
import org.osgl.util.C;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

class ScopedFactory extends Module {
    final Map<String, Object> registry = C.newMap();

    private static final ScopeCache.SessionScope SESSION_SCOPE = new ScopeCache.SessionScope() {
        @Override
        public <T> T get(String key) {
            Context context = Context.get();
            return context.get(key);
        }

        @Override
        public <T> void put(String key, T bean) {
            Context context = Context.get();
            context.put(key, bean);
        }
    };

    static final Provider<ScopeCache.SessionScope> SESSION_SCOPE_PROVIDER = new Provider<ScopeCache.SessionScope>() {
        @Override
        public ScopeCache.SessionScope get() {
            return SESSION_SCOPE;
        }
    };

    @Override
    protected void configure() {
        bind(ScopeCache.SingletonScope.class).to(new Provider<ScopeCache.SingletonScope>() {
            @Override
            public ScopeCache.SingletonScope get() {
                return new ScopeCache.SingletonScope() {
                    @Override
                    public <T> T get(String key) {
                        return (T) registry.get(key);
                    }

                    @Override
                    public <T> void put(String key, T bean) {
                        registry.put(key, bean);
                    }
                };
            }
        });
        bind(ScopeCache.SessionScope.class).to(SESSION_SCOPE_PROVIDER);

        bind(SingletonBoundObject.class).to(SingletonBean.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public static SingletonProduct createSingleton(SingletonBean bean) {
        return bean;
    }

    @Provides
    @SessionScoped
    public static SessionProduct createSession(SessionBean bean) {
        return bean;
    }

}
