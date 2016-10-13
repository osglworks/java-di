package org.osgl.inject;

import org.osgl.$;

import java.util.Map;

/**
 * Implementation shall provide the logic that can
 * transform a bean instance (no type change)
 *
 * @param <E> the generic type of element
 */
public interface BeanTransformer<E> {

    /**
     * Create a filter function with the hint and options specified. This could
     * be used to produce composite Bean loader based on other bean loaders
     *
     * @param options   the optional parameters specified to refine the filtering logic
     * @param bean the bean spec of the bean to be injected
     * @return a function that transform the bean instance (e.g. convert a string to uppercase etc)
     */
    $.Function<E, E> filter(Map<String, Object> options, BeanSpec bean);

}
