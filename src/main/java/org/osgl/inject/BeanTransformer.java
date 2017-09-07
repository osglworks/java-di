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
 * transform a bean instance (no type change).
 *
 * @param <E> the generic type of element
 */
public interface BeanTransformer<E> {

    /**
     * Create a filter function with the hint and options specified. This could
     * be used to produce composite Bean loader based on other bean loaders.
     *
     * @param options
     *      the optional parameters specified to refine the filtering logic
     * @param bean
     *      the bean spec of the bean to be injected
     * @return
     *      a function that transform the bean instance
     *      (e.g. convert a string to uppercase etc)
     */
    $.Function<E, E> filter(Map<String, Object> options, BeanSpec bean);

}
