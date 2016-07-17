package org.osgl.genie;

import java.util.List;
import java.util.Map;

/**
 * Define a generic interface to implement bean instance loading mechanisms
 */
public interface BeanLoader<T> extends BeanFilter<T>, Comparable<BeanLoader<T>> {
    /**
     * Load a bean based on the `hint` specified.
     *
     * It is up to the implementation to decide how to use the `hint`
     *
     * The bean instance returned must pass the test of the function
     * returned by {@link #filter(Object, Map)}
     *
     * @param hint the hint to specify the bean to be loaded
     * @param options optional parameters specified to refine the loading process
     * @return the bean instance
     */
    T loadOne(Object hint, Map<String, Object> options);

    /**
     * Load multiple beans based on the `hint` specified.
     *
     * It is up to the implementation to decide how to use the `hint`
     *
     * **Note** it is important for implementation to set the limit of
     * number of instances to be loaded in conjunction with the `hint`.
     *
     * All bean instances loaded must pass the test of the function returned
     * by {@link #filter(Object, Map)}
     *
     *
     * @param hint the hint to specify the bean instances to be loaded
     * @param options optional parameters specified to refine the loading process
     * @return a list of bean instances
     */
    List<T> loadMultiple(Object hint, Map<String, Object> options);


    /**
     * When multiple `BeanLoader` are used together to populate the data series, the
     * `priority()` method can used to determine which loader is called first. The
     * lower the value, the higher the priority is, meaning the loader is sorted in
     * ascending order based on `priority()` value.
     *
     * The gist is if a loader loads fewer number of instance, then the priority value shall
     * be lower than that of the loader which loads more instances.
     *
     * @return prority of the loader
     */
    int priority();
}
