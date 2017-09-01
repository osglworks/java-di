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
     *
     * @param options   the optional parameters specified to refine the filtering logic
     * @param container the bean spec of the container into which the element will be loaded
     * @return a filter to check if a certain bean instance matches this bean loader specification
     */
    $.Function<E, Boolean> filter(Map<String, Object> options, BeanSpec container);

}
