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

import org.osgl.inject.Genie.Binder;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.util.*;

public abstract class Module {

    private List<Binder> binders = new ArrayList<Binder>();
    private Set<Class<? extends Annotation>> qualifiers = new HashSet<Class<? extends Annotation>>();
    private Map<Class<?>, GenericTypedBeanLoader<?>> genericTypedBeanLoaders = new HashMap<Class<?>, GenericTypedBeanLoader<?>>();
    private boolean configured;

    protected final <T> Binder<T> bind(Class<T> type) {
        Binder<T> binder = new Binder<T>(type);
        binders.add(binder);
        return binder;
    }

    protected final Module registerQualifiers(Class<? extends Annotation> ... qualifiers) {
        this.qualifiers.addAll(C.listOf(qualifiers));
        return this;
    }

    protected final <T> void registerGenericTypedBeanLoader(Class<T> type, GenericTypedBeanLoader<T> loader) {
        genericTypedBeanLoaders.put(type, loader);
    }

    protected abstract void configure();

    final void applyTo(Genie genie) {
        if (!configured) {
            configure();
        }
        configured = true;
        validate(genie);
        for (Binder<?> binder : binders) {
            binder.register(genie);
        }
        genie.registerQualifiers(qualifiers);
        for (Map.Entry<Class<?>, GenericTypedBeanLoader<?>> entry : genericTypedBeanLoaders.entrySet()) {
            genie.registerGenericTypedBeanLoader(entry.getKey(), (GenericTypedBeanLoader) entry.getValue());
        }
    }

    private void validate(Genie genie) {
        Map<Object, Binder> map = C.newMap();
        for (Binder<?> binder : binders) {
            Object spec = binder.beanSpec(genie);
            if (map.containsKey(spec)) {
                throw E.invalidConfiguration("Duplicate bean spec found: ", spec);
            }
            map.put(spec, binder);
        }
    }

}
