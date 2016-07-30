package org.osgl.inject;

import java.util.Map;

/**
 * An `ValueLoader` is used to load bean (value) in place. This could be used
 * to load simple values like `int`, `String` etc. It can also be used by
 * container to load complex object marshaled from wire, e.g. a post request
 * data
 *
 * @param <T> the generic element type
 */
public interface ValueLoader<T> {

    /**
     * Provide element data to be loaded
     *
     * @param options   options that could be used to regulate the data loading logic
     * @param spec the bean spec about the data to be loaded
     * @return an {@link Iterable} of elements
     */
    T load(Map<String, Object> options, BeanSpec spec);

}
