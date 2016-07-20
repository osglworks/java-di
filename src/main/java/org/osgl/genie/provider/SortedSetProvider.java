package org.osgl.genie.provider;

import javax.inject.Provider;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Inject {@link SortedSet} using {@link TreeSet}
 */
public class SortedSetProvider implements Provider<SortedSet<?>> {

    public static final SortedSetProvider INSTANCE = new SortedSetProvider();

    @Override
    public SortedSet<?> get() {
        return new TreeSet();
    }
}
