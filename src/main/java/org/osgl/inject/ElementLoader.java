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

import java.util.Collection;
import java.util.Map;

/**
 * An `ElementLoader` is responsible for providing initial data
 * to be loaded into a {@link Collection} type.
 *
 * @param <E> the generic element type
 */
public interface ElementLoader<E> extends ElementFilter<E> {

    /**
     * Provide element data to be loaded.
     *
     * @param options
     *      options that could be used to regulate the data loading logic
     * @param container
     *      the bean spec about the container into which the element will be loaded
     * @param genie
     *      the dependency injector that could be used to recursively load dependencies
     * @return
     *      an {@link Iterable} of elements
     */
    Iterable<E> load(Map<String, Object> options, BeanSpec container, Genie genie);

    /**
     * When multiple `ElementLoader` are used together to load initial data, the
     * `priority()` method can used to determine which loader is called first.
     * All following loaders will be treated as {@link ElementFilter} to filter
     * the data series generated by the first loader.
     * <p>
     * A good practice is if a loader loads fewer number of instance,
     * then the priority value shall be lower than that of the loader
     * which loads more instances.
     *
     * @return
     *      priority of the loader
     */
    int priority();

}
