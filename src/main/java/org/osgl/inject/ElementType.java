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
import org.osgl.util.C;

import java.util.List;

/**
 * Used in {@link org.osgl.inject.annotation.AnnotatedWith} and
 * {@link org.osgl.inject.annotation.TypeOf} annotation to specify
 * the type of the element should be returned by
 * {@link org.osgl.inject.loader.TypedElementLoader} and
 * {@link org.osgl.inject.loader.AnnotatedElementLoader} respectively.
 */
public enum ElementType {
    /**
     * Specify the element loader shall return the Class found.
     */
    CLASS() {
        @Override
        public List<Object> transform(List<Class<?>> classes, Genie genie) {
            return (List)classes;
        }
    },

    /**
     * Specify the element loader shall return the bean instantiated.
     */
    BEAN() {
        @Override
        public List<Object> transform(List<Class<?>> classes, final Genie genie) {
            return C.newList(classes).map(new $.Transformer<Class, Object>() {
                @Override
                public Object transform(Class clazz) {
                    return genie.get(clazz);
                }
            });
        }
    };

    /**
     * Transform a list of classes into required elements.
     *
     * It will always return the passed in class list for
     * {@link #CLASS} element type
     *
     * For {@link #BEAN} it will return list of the instantiated
     * instance from the class list using genie the dependency
     * injector passed in
     *
     * @param classes
     *      the class list
     * @param genie
     *      the injector
     * @return
     *      transformed list
     */
    public abstract List<Object> transform(List<Class<?>> classes, Genie genie);

    /**
     * Specify whether it should load abstract class.
     * @return
     *      `true` if this element type is not {@link #BEAN}
     */
    public boolean loadAbstract() {
        return this != BEAN;
    }
}
