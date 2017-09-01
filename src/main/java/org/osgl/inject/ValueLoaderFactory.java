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
import org.osgl.inject.annotation.LoadValue;
import org.osgl.util.E;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * `ValueLoaderFactory` load the bean instance directly from
 * {@link ValueLoader} based on the option data specified in
 * {@link org.osgl.inject.annotation.LoadValue} annotation
 */
class ValueLoaderFactory {

    static <T> Provider<T> create(BeanSpec spec, Genie genie) {
        Annotation anno = spec.valueLoader();
        E.illegalArgumentIf(null == anno);
        Map<String, Object> options = $.evaluate(anno);
        Class<? extends Annotation> annoType = anno.annotationType();
        LoadValue loadValue;
        if (LoadValue.class == annoType) {
            loadValue = (LoadValue) anno;
        } else {
            loadValue = annoType.getAnnotation(LoadValue.class);
        }
        ValueLoader<T> valueLoader = genie.get(loadValue.value());
        valueLoader.init(options, spec);
        return valueLoader;
    }

}
