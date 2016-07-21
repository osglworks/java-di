package org.osgl.genie.provider;

import org.osgl.util.C;

import javax.inject.Provider;

/**
 * Inject {@link java.util.Set} using {@link org.osgl.util.DelegatingSet}
 */
public class OsglSetProvider implements Provider<C.Set<?>> {

    public static final OsglSetProvider INSTANCE = new OsglSetProvider();

    @Override
    public C.Set<?> get() {
        return C.newSet();
    }

}
