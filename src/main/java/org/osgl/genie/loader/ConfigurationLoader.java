package org.osgl.genie.loader;

import org.osgl.Osgl;

import java.util.List;
import java.util.Map;

/**
 * A `ConfigurationLoader` implementation should load the bean from
 * an application's configuration
 */
public abstract class ConfigurationLoader extends ElementLoaderBase<Object> {

    /**
     * Load bean from configuration specified by `hint`
     * @param options not used
     * @return the configuration value
     */
    @Override
    public abstract Iterable<Object> load(Map<String, Object> options);

    @Override
    public int priority() {
        return 0;
    }

    /**
     * `ConfigurationLoader` shall **NOT** be used in conjunction with other
     * {@link org.osgl.genie.ElementLoader element loaders}. Thus it suppose this method
     * will never get called.
     *
     * In case this method is called, it will throw out {@link UnsupportedOperationException}
     *
     * @param options not used
     * @return Nil
     * @throws UnsupportedOperationException
     */
    @Override
    public final Osgl.Function<Object, Boolean> filter(Map<String, Object> options) {
        throw new UnsupportedOperationException();
    }
}
