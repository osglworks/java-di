package org.osgl.genie.loader;

import org.osgl.Osgl;

import java.util.List;
import java.util.Map;

/**
 * A `ConfigurationLoader` implementation should load the bean from
 * an application's configuration
 */
public abstract class ConfigurationLoader extends BeanLoaderBase<Object> {

    /**
     * Load bean from configuration specified by `hint`
     * @param hint the configuration key
     * @param options not used
     * @return the configuration value
     */
    @Override
    public abstract Object loadOne(Object hint, Map<String, Object> options);

    /**
     * Load configuration value as list. If the configuration corresponding to
     * `hint` is not a list type, then this method will return a single element
     * list that contains the configuration value.
     *
     * @param hint the configuration key
     * @param options it is up to implementation to decide how to use the options
     * @return a list of configuration value
     */
    @Override
    public abstract List<Object> loadMultiple(Object hint, Map<String, Object> options);


    /**
     * `ConfigurationLoader` shall **NOT** be used in conjunction with other
     * {@link org.osgl.genie.BeanLoader bean loaders}. Thus it suppose this method
     * will never get called.
     *
     * In case this method is called, it will throw out {@link UnsupportedOperationException}
     *
     * @param hint the configuration key, not relevant in this method
     * @param options not used
     * @return Nil
     * @throws UnsupportedOperationException
     */
    @Override
    public final Osgl.Function<Object, Boolean> filter(Object hint, Map<String, Object> options) {
        throw new UnsupportedOperationException();
    }
}
