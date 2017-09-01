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


import org.osgl.inject.loader.AnnotatedElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark the type of elements of a field or method parameter should
 * be annotated with specified annotation
 */
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@LoadCollection(AnnotatedElementLoader.class)
public @interface AnnotatedWith {
    /**
     * Specify the annotation class
     *
     * @return the annotation class
     */
    Class<?> value();

    /**
     * Specify the type of element the loader should return
     * @return the element type
     */
    org.osgl.inject.ElementType elementType() default org.osgl.inject.ElementType.BEAN;

    /**
     * Should the loader load non-public class or not
     * @return `true` or `false` as described above
     */
    boolean loadNonPublic() default false;

    /**
     * Should the loader load abstract class or not
     *
     * **Note** the value of `loadAbstract` will be ignored if
     * {@link #elementType()} is set to {@link org.osgl.inject.ElementType#BEAN}
     *
     * @return `true` or `false` as described above.
     */
    boolean loadAbstract() default false;

}
