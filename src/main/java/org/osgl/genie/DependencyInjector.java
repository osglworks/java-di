package org.osgl.genie;

public interface DependencyInjector {
    /**
     * Create an instance of type T using the class of type T
     *
     * @param clazz the class
     * @param <T>   the generic type
     * @return the bean of class specified
     */
    <T> T get(Class<T> clazz);
}
