package org.osgl.genie;

import org.osgl.$;

import java.util.Map;

/**
 * Implementation shall provide the logic that can
 * filter a bean instance
 *
 * @param <E> the generic type of element
 */
public interface ElementFilter<E> {

    /**
     * Create a filter function with the hint and options specified. This could
     * be used to produce composite Bean loader based on other bean loaders
     * @param options the optional parameters specified to refine the filtering logic
     * @return a filter to check if a certain bean instance matches this bean loader specification
     */
    $.Function<E, Boolean> filter(Map<String, Object> options);

}
