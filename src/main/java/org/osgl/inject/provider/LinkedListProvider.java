package org.osgl.inject.provider;

import javax.inject.Provider;
import java.util.LinkedList;

/**
 * Inject {@link LinkedList} and {@link LinkedList} using
 * {@link org.osgl.util.DelegatingList} implementation.
 */
public class LinkedListProvider implements Provider<LinkedList<?>> {

    public static final LinkedListProvider INSTANCE = new LinkedListProvider();

    @Override
    public LinkedList<?> get() {
        return new LinkedList<Object>();
    }

}
