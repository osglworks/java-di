package org.osgl.genie.provider;

import org.osgl.util.C;

import javax.inject.Provider;
import java.util.List;

/**
 * Inject {@link List} and {@link org.osgl.util.C.List} using
 * {@link org.osgl.util.DelegatingList} implementation.
 */
public class OsglListProvider implements Provider<C.List<?>> {

    public static final OsglListProvider INSTANCE = new OsglListProvider();

    @Override
    public C.List<?> get() {
        return C.newList();
    }

}
