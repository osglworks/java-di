package org.osgl.inject.provider;

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
import org.osgl.inject.Genie;
import org.osgl.inject.util.ArrayLoader;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class ArrayProvider implements Provider {
    protected final Class elementType;
    protected final BeanSpec listSpec;
    protected final Genie genie;


    private ArrayProvider(Class elementType, Genie genie) {
        this.elementType = elementType;
        this.listSpec = BeanSpec.of(ArrayList.class, null, genie);
        this.genie = genie;
    }

    private ArrayProvider(Class elementType, BeanSpec listSpec, Genie genie) {
        this.elementType = elementType;
        this.listSpec = listSpec;
        this.genie = genie;
    }

    @Override
    public Object get() {
        ArrayList list = genie.get(listSpec);
        return listToArray(list);
    }

    private Object listToArray(List list) {
        return ArrayLoader.listToArray(list, elementType);
    }

    public static ArrayProvider of(Class arrayClass, Genie genie) {
        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Array class expected");
        }
        return new ArrayProvider(arrayClass.getComponentType(), genie);
    }

    public static ArrayProvider of(BeanSpec beanSpec, Genie genie) {
        if (!beanSpec.isArray()) {
            throw new IllegalArgumentException("Array bean spec required");
        }
        Class arrayClass = beanSpec.rawType();
        return new ArrayProvider(arrayClass.getComponentType(), beanSpec.toList(), genie);
    }



}
