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

import org.osgl.$;
import org.osgl.inject.Injector;

import javax.inject.Provider;

/**
 * A lazy provider is prepared with the bean class and initialize
 * the bean upon demand
 */
public class LazyProvider<T> implements Provider<T> {

    private Class<? extends T> clazz;
    private Injector injector;

    public LazyProvider(Class<? extends T> clazz, Injector injector) {
        this.clazz = $.requireNotNull(clazz);
        this.injector = $.requireNotNull(injector);
    }


    @Override
    public T get() {
        return injector.get(clazz);
    }

}
