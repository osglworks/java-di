package org.osgl.genie;

import org.osgl.genie.loader.TypedBeanLoader;
import org.osgl.util.C;

import java.util.List;

/**
 * Emulate Type discovery mechanism in IoC container
 */
public class SimpleTypeBeanLoader extends TypedBeanLoader {
    @Override
    protected List load(Class type) {
        if (type == ErrorHandler.class) {
            return C.list(new NotFoundHandler(), new InternalErrorHandler());
        }
        return C.list();
    }
}
