package org.osgl.inject.provider;

import org.osgl.util.C;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Inject {@link List} and {@link C.List} using
 * {@link org.osgl.util.DelegatingList} implementation.
 */
public class ArrayListProvider implements Provider<ArrayList<?>> {

    public static final ArrayListProvider INSTANCE = new ArrayListProvider();

    @Override
    public ArrayList<?> get() {
        return new ArrayList<Object>();
    }

}
