package org.osgl.genie.provider;

import javax.inject.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Inject {@link java.util.concurrent.ConcurrentMap} with {@link ConcurrentHashMap}
 * implementation
 */
public class ConcurrentMapProvider implements Provider<ConcurrentMap<?, ?>> {

    public static final ConcurrentMapProvider INSTANCE = new ConcurrentMapProvider();

    @Override
    public ConcurrentMap<?, ?> get() {
        return new ConcurrentHashMap();
    }
}
