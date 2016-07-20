package org.osgl.genie.provider;

import javax.inject.Provider;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Inject {@link Deque} type bean using {@link ArrayDeque}
 */
public class DequeProvider implements Provider<Deque<?>> {

    public static final DequeProvider INSTANCE = new DequeProvider();

    @Override
    public Deque<?> get() {
        return new ArrayDeque();
    }

}
