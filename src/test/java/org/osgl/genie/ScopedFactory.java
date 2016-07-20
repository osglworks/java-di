package org.osgl.genie;

import org.osgl.genie.ScopedObjects.*;
import org.osgl.genie.annotation.Provides;
import org.osgl.genie.annotation.SessionScoped;
import org.osgl.util.C;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

public class ScopedFactory extends Module {
    final Map<Class, Object> registry = C.newMap();

    @Override
    protected void configure() {
        bind(ScopeCache.SingletonScope.class).to(new Provider<ScopeCache.SingletonScope>() {
            @Override
            public ScopeCache.SingletonScope get() {
                return new ScopeCache.SingletonScope() {
                    @Override
                    public <T> T get(Class<T> clazz) {
                        return (T) registry.get(clazz);
                    }

                    @Override
                    public <T> void put(Class<T> clazz, T bean) {
                        registry.put(clazz, bean);
                    }
                };
            }
        });
        bind(ScopeCache.SessionScope.class).to(new Provider<ScopeCache.SessionScope>() {
            @Override
            public ScopeCache.SessionScope get() {
                return new ScopeCache.SessionScope() {
                    @Override
                    public <T> T get(Class<T> clazz) {
                        Context context = Context.get();
                        return context.get(clazz.getName());
                    }

                    @Override
                    public <T> void put(Class<T> clazz, T bean) {
                        Context context = Context.get();
                        context.put(clazz.getName(), bean);
                    }
                };
            }
        });

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
