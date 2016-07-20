package org.osgl.genie.provider;

import javax.inject.Provider;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Inject {@link SortedMap} using {@link TreeMap}
 */
public class SortedMapProvider implements Provider<SortedMap<?, ?>> {

    public static final SortedMapProvider INSTANCE = new SortedMapProvider();

    @Override
    public SortedMap<?, ?> get() {
        return new TreeMap();
    }
}
