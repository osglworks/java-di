package org.osgl.genie.loader;

import org.osgl.genie.BeanLoader;

/**
 * Base class for {@link BeanLoader} implementations
 */
public abstract class BeanLoaderBase<T> implements BeanLoader<T> {

    /**
     * The default loader priority is set to `5`
     * @return the default priority value: `5`
     */
    @Override
    public int priority() {
        return 5;
    }

    @Override
    public final int compareTo(BeanLoader<T> o) {
        return o.priority() - priority();
    }
}
