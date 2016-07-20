package org.osgl.genie;

import org.osgl.genie.annotation.RequestScoped;
import org.osgl.genie.annotation.SessionScoped;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Decorate on a {@link javax.inject.Provider} with scope cache
 * checking function
 */
class ScopedProvider<T> implements Provider<T> {

    private Provider<T> realProvider;
    private Class<T> targetClass;
    private ScopeCache cache;

    private ScopedProvider(Class<T> targetClass, ScopeCache cache, Provider<T> realProvider) {
        this.targetClass = targetClass;
        this.realProvider = realProvider;
        this.cache = cache;
    }

    @Override
    public T get() {
        T bean = cache.get(targetClass);
        if (null == bean) {
            bean = realProvider.get();
        }
        cache.put(targetClass, bean);
        return bean;
    }

    static <T> Provider<T> decorate(Genie.Key key, Provider<T> realProvider, Genie genie) {
        if (realProvider instanceof ScopedProvider) {
            return realProvider;
        }
        Class<T> targetClass = key.rawType();
        ScopeCache cache = resolve(key.scope(), genie);

        return null == cache ? realProvider : new ScopedProvider<T>(targetClass, cache, realProvider);
    }

    static ScopeCache resolve(Class<? extends Annotation> annoClass, Genie genie) {
        if (Singleton.class == annoClass) {
            return genie.get(ScopeCache.SingletonScope.class);
        } else if (RequestScoped.class == annoClass) {
            return genie.get(ScopeCache.RequestScope.class);
        } else if (SessionScoped.class == annoClass) {
            return genie.get(ScopeCache.SessionScope.class);
        }
        return null;
    }
}
