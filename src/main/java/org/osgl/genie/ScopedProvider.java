package org.osgl.genie;

import org.osgl.genie.annotation.RequestScoped;
import org.osgl.genie.annotation.SessionScoped;

import javax.inject.Provider;
import javax.inject.Singleton;
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
        ScopeCache cache = null;
        if (targetClass.isAnnotationPresent(Singleton.class)) {
            cache = genie.get(ScopeCache.SingletonScope.class);
        } else if (targetClass.isAnnotationPresent(RequestScoped.class)) {
            cache = genie.get(ScopeCache.RequestScope.class);
        } else if (targetClass.isAnnotationPresent(SessionScoped.class)) {
            cache = genie.get(ScopeCache.SessionScope.class);
        }

        return null == cache ? realProvider : new ScopedProvider<T>(targetClass, cache, realProvider);
    }
}
