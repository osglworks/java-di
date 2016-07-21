package org.osgl.genie;

import org.osgl.genie.loader.TypedElementLoader;
import org.osgl.util.C;

import java.util.List;

/**
 * Emulate Type discovery mechanism in IoC container
 */
public class SimpleTypeElementLoader extends TypedElementLoader {
    @Override
    protected List load(Class type) {
        if (type == ErrorHandler.class) {
            return C.list(NotFoundHandler.class, InternalErrorHandler.class);
        }
        return C.list();
    }
}
