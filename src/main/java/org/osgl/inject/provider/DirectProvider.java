package org.osgl.inject.provider;

import org.osgl.$;

import javax.inject.Provider;

/**
 * A Simple implementation of {@link javax.inject.Provider} that provide the bean
 * prepared before hand
 */
public class DirectProvider<T> implements Provider<T> {

    public T bean;

    public DirectProvider(T bean) {
        this.bean = $.notNull(bean);
    }

    @Override
    public T get() {
        return bean;
    }
}
