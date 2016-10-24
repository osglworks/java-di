package org.osgl.inject.provider;

import org.osgl.$;
import org.osgl.inject.Injector;

import javax.inject.Provider;

/**
 * A lazy provider is prepared with the bean class and initialize
 * the bean upon demand
 */
public class LazyProvider<T> implements Provider<T> {

    private Class<? extends T> clazz;
    private Injector injector;

    public LazyProvider(Class<? extends T> clazz, Injector injector) {
        this.clazz = $.notNull(clazz);
        this.injector = $.notNull(injector);
    }


    @Override
    public T get() {
        return injector.get(clazz);
    }

}
