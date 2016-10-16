package org.osgl.inject;

import javax.inject.Provider;
import java.util.Map;

/**
 * An `ValueLoader` is used to load bean (value) in place. This could be used
 * to load simple values like `int`, `String` etc. It can also be used by
 * container to load complex object marshaled from wire, e.g. a post request
 * data
 *
 * @param <T> the generic element type
 */
public interface ValueLoader<T> extends Provider<T> {

     /**
     * Initialize the value loader with options and bean spec
     *
     * @param options   options that could be used to regulate the data loading logic
     * @param spec the bean spec about the data to be loaded
     */
    void init(Map<String, Object> options, BeanSpec spec);

    /**
     * Provide element data to be loaded using the options and bean spec
     * initialized by {@link #init(Map, BeanSpec)}
     *
     * @return an {@link Iterable} of elements
     */
    @Override
    T get();

    abstract class Base<T> implements ValueLoader<T> {

        protected Map<String, Object> options;
        protected BeanSpec spec;

        @Override
        public final void init(Map options, BeanSpec spec) {
            this.options = options;
            this.spec = spec;
            this.initialized();
        }

        /**
         * Extend class can do further configuration here with
         * all information of options and bean spec has been set
         */
        protected void initialized() {}

        /**
         * Return the `value` of the annotation from {@link #options} map
         * @param <V> the generic type of value
         * @return the value or `null` if not found
         */
        protected <V> V value() {
            return (V) options.get("value");
        }
    }

}
