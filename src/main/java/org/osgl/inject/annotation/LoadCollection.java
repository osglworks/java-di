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

import org.osgl.inject.BeanSpec;
import org.osgl.inject.ElementLoader;

import java.lang.annotation.*;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Used to tag an annotation with {@link ElementLoader collection element loader}
 * specification. Annotations tagged with `LoadCollection` is used to mark
 * a {@link java.util.Collection} or {@link java.util.Map} type
 * inject target needs additional logic to load element data
 */
@Documented
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ANNOTATION_TYPE, FIELD, PARAMETER})
public @interface LoadCollection {
    /**
     * Specify the {@link ElementLoader} implementation used to
     * load bean(s)
     *
     * @return the `ElementLoader` implementation
     */
    Class<? extends ElementLoader> value();

    /**
     * Indicate it shall reverse the {@link org.osgl.inject.ElementFilter#filter(Map, BeanSpec) filter function}
     * when loading element from collection
     */
    boolean reverseFilter() default false;
}
