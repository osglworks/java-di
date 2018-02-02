package org.osgl.inject;

/**
 * A `NamedProvider` can be used to get an instance with a given name.
 */
public interface NamedProvider<T> {
    /**
     * Return an instance with a given name
     * @param name the name
     * @return an instance corresponding to the name
     */
    T get(String name);
}
