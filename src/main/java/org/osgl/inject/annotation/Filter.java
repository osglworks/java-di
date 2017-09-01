package org.osgl.inject.annotation;

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

import org.osgl.inject.ElementFilter;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ElementFilter bean filter}
 * specification.
 *
 * This annotation can be used in conjunction with {@link LoadCollection}
 * to filter the element to be loaded into a collection typed bean
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Filter {
    /**
     * Specify the {@link ElementFilter} implementation used to
     * filter bean(s) loaded by {@link org.osgl.inject.ElementLoader bean loaders}
     *
     * @return the `ElementFilter` class
     */
    Class<? extends ElementFilter> value();

    /**
     * Specify it shall reverse the filter function when applying the filter
     */
    boolean reverse() default false;
}
