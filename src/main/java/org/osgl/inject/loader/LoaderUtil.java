package org.osgl.inject.loader;

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
import org.osgl.inject.BeanSpec;
import org.osgl.inject.ElementType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

class LoaderUtil {
    static Class<?> targetClass($.Var<ElementType> typeVar, BeanSpec container) {
        List<Type> types = container.typeParams();
        Class targetClass = null;
        if (!types.isEmpty()) {
            // the effective type is always the last one
            // this is for both Collection and Map
            Type type = types.get(types.size() - 1);
            if (type instanceof Class) {
                targetClass = $.cast(type);
                if (targetClass == Class.class) {
                    typeVar.set(ElementType.CLASS);
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType ptype = $.cast(type);
                if (ptype.getRawType() instanceof Class) {
                    type = ptype.getActualTypeArguments()[0];
                    if (type instanceof Class) {
                        targetClass = $.cast(type);
                        typeVar.set(ElementType.CLASS);
                    }
                }
            }
        }
        return targetClass;
    }
}
