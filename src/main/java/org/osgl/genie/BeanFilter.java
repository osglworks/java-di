package org.osgl.genie;

import org.osgl.$;

import java.util.Map;

/**
 * Implementation shall provide the logic that can
 * filter a bean instance tuned with the `hint`
 */
public interface BeanFilter<T> {

    /**
     * Create a filter function with the hint and options specified. This could
     * be used to produce composite Bean loader based on other bean loaders
     * @param hint the hint to specify the bean instances to be loaded
     * @param options the optional parameters specified to refine the loading process
     * @return a filter to check if a certain bean instance matches this bean loader specification
     */
    $.Function<T, Boolean> filter(Object hint, Map<String, Object> options);

}
