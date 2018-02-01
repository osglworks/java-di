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

import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;

import java.lang.annotation.Annotation;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Decorate on a {@link javax.inject.Provider} with scope cache
 * checking function.
 */
class ScopedProvider<T> implements Provider<T> {

    private Provider<T> realProvider;
    private BeanSpec target;
    private ScopeCache cache;

    private ScopedProvider(BeanSpec target, ScopeCache cache, Provider<T> realProvider) {
        this.target = target;
        this.realProvider = realProvider;
        this.cache = cache;
    }

    @Override
    public T get() {
        T bean = cache.get(target);
        if (null == bean) {
            bean = realProvider.get();
            cache.put(target, bean);
        }
        return bean;
    }

    static <T> Provider<T> decorate(BeanSpec spec, Provider<T> realProvider, Genie genie) {
        if (realProvider instanceof ScopedProvider) {
            return realProvider;
        }
        ScopeCache cache = resolve(spec.scope(), genie);

        return null == cache ? realProvider : new ScopedProvider<>(spec, cache, realProvider);
    }

    static ScopeCache resolve(Class<? extends Annotation> annoClass, Genie genie) {
        ScopeCache cache = resolveBuiltIn(annoClass, genie);
        if (null != cache) {
            return cache;
        }
        Class<? extends Annotation> alias = genie.scopeByAlias(annoClass);
        if (null != alias) {
            cache = resolveBuiltIn(alias, genie);
        }
        if (null == cache) {
            cache = genie.scopeCache(annoClass);
        }
        return cache;
    }

    private static ScopeCache resolveBuiltIn(Class<? extends Annotation> annoClass, Genie genie) {
        if (Singleton.class == annoClass) {
            return genie.get(ScopeCache.SingletonScope.class);
        } else if (RequestScoped.class == annoClass) {
            return genie.get(ScopeCache.RequestScope.class);
        } else if (SessionScoped.class == annoClass) {
            return genie.get(ScopeCache.SessionScope.class);
        }
        return null;
    }
}
