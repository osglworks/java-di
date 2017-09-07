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


import org.osgl.inject.loader.TypedElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark elements of a field or method parameter should be
 * type of specified base class or interface.
 *
 * @see LoadCollection
 */
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@LoadCollection(TypedElementLoader.class)
public @interface TypeOf {
    /**
     * Specify the base class or interface.
     * <p>
     * Default value is {@link TypeOf.PlaceHolder}. When default value
     * is used it directs the genie to use element' type
     * parameter
     *
     * @return the base class or interface
     */
    Class<?> value() default PlaceHolder.class;

    final class PlaceHolder {
    }

    /**
     * Specify the type of element the loader should return
     *
     * **Note** when the generic type of element is `Class<...>` the
     * value of elementType will always be treated as {@link org.osgl.inject.ElementType#CLASS}
     * and user setting through this property will be ignored
     *
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

    /**
     * Should the loader load Class/Bean using the class specified by {@link #value()}.
     * If specified as `false` then it shall load purely sub type of the type specified, otherwise
     * it might load the specified type if:
     *
     * 1. {@link #elementType()} is {@link org.osgl.inject.ElementType#CLASS}
     * 2. {@link #elementType()} is {@link org.osgl.inject.ElementType#BEAN} and the specified
     *    type is non-static class and can initialized
     * @return `true` or `false` as described above
     */
    boolean loadRoot() default false;
}
