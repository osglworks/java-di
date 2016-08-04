package org.osgl.inject.provider;

import javax.inject.Provider;

/**
 * A provider that always returns `null`
 */
public class NullValueProvider implements Provider {

    public static final Provider INSTANCE = new NullValueProvider();

    private NullValueProvider() {}

    @Override
    public Object get() {
        return null;
    }

    public static <T> Provider<T> instance() {
        return INSTANCE;
    }
}
