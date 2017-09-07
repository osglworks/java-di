package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Provide function to retrieve bean from scoped cache.
 */
public interface ScopeCache {
    /**
     * Get a bean from the cache defined in the scope.
     *
     * @param clazz
     *      the key to retrieve the bean
     * @param <T>
     *      generic type of the bean
     * @return
     *      the bean instance
     */
    <T> T get(Class<T> clazz);

    /**
     * Put a bean instance into the cache associated with the class key
     * specified.
     *
     * @param clazz
     *      the key to store the bean instance
     * @param bean
     *      the bean instance to be stored
     * @param <T>
*           generic type of the bean
     */
    <T> void put(Class<T> clazz, T bean);

    /**
     * Implementation of `ScopeCache.SingletonScope` provide access
     * to bean instances stored in a singleton registry.
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
