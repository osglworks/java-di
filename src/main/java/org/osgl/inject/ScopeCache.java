package org.osgl.inject;

/**
 * Provide function to retrieve bean from scoped cache
 */
public interface ScopeCache {
    /**
     * Get a bean from the cache defined in the scope
     *
     * @param clazz the key to retrieve the bean
     * @param <T>   generic type of the bean
     * @return the bean instance
     */
    <T> T get(Class<T> clazz);

    /**
     * Put a bean instance into the cache associated with the class key
     * specified
     *
     * @param clazz the key to store the bean instance
     * @param bean  the bean instance to be stored
     * @param <T>   generic type of the bean
     */
    <T> void put(Class<T> clazz, T bean);

    /**
     * Implementation of `ScopeCache.SingletonScope` provide access
     * to bean instances stored in a singleton registry
     */
    interface SingletonScope extends ScopeCache {
    }

    /**
     * Implementation of `ScopeCache.RequestScope` shall provide access
     * to bean instance stored in a request scope
     */
    interface RequestScope extends ScopeCache {
    }

    /**
     * Implementation of `ScopeCache.RequestScope` shall provide access
     * to bean instance stored in a session scope
     */
    interface SessionScope extends ScopeCache {
    }

}
