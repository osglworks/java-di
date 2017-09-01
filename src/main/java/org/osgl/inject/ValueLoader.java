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

import java.util.Map;
import javax.inject.Provider;

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
     * Initialize the value loader with options and bean spec.
     *
     * @param options options that could be used to regulate the data loading logic
     * @param spec    the bean spec about the data to be loaded
     */
    void init(Map<String, Object> options, BeanSpec spec);

    /**
     * Provide element data to be loaded using the options and bean spec
     * initialized by {@link #init(Map, BeanSpec)}.
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
         * all information of options and bean spec has been set.
         */
        protected void initialized() {
        }

        /**
         * Return the `value` of the annotation from {@link #options} map.
         *
         * @param <V> the generic type of value
         * @return the value or `null` if not found
         */
        protected <V> V value() {
            return (V) options.get("value");
        }
    }

}
