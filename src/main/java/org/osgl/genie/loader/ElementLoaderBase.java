package org.osgl.genie.loader;

import org.osgl.genie.ElementLoader;

/**
 * Base class for {@link ElementLoader} implementations
 */
public abstract class ElementLoaderBase<T> implements ElementLoader<T> {

    /**
     * The default loader priority is set to `5`
     *
     * @return the default priority value: `5`
     */
    @Override
    public int priority() {
        return 5;
    }

}
