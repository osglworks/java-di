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

import org.osgl.inject.ScopedObjects.*;
import org.osgl.inject.annotation.Provides;
import org.osgl.inject.annotation.SessionScoped;
import org.osgl.util.C;

import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;

class ScopedFactory extends Module {
    final Map<BeanSpec, Object> registry = C.newMap();

    private static final ScopeCache.SessionScope SESSION_SCOPE = new ScopeCache.SessionScope() {
        @Override
        public <T> T get(BeanSpec target) {
            Context context = Context.get();
            return context.get(target.toString());
        }

        @Override
        public <T> void put(BeanSpec target, T bean) {
            Context context = Context.get();
            context.put(target.toString(), bean);
        }
    };

    static final Provider<ScopeCache.SessionScope> SESSION_SCOPE_PROVIDER = new Provider<ScopeCache.SessionScope>() {
        @Override
        public ScopeCache.SessionScope get() {
            return SESSION_SCOPE;
        }
    };

    @Override
    protected void configure() {
        bind(ScopeCache.SingletonScope.class).to(new Provider<ScopeCache.SingletonScope>() {
            @Override
            public ScopeCache.SingletonScope get() {
                return new ScopeCache.SingletonScope() {
                    @Override
                    public <T> T get(BeanSpec target) {
                        return (T) registry.get(target);
                    }

                    @Override
                    public <T> void put(BeanSpec target, T bean) {
                        registry.put(target, bean);
                    }
                };
            }
        });
        bind(ScopeCache.SessionScope.class).to(SESSION_SCOPE_PROVIDER);
        bind(SingletonBoundObject.class).to(SingletonBean.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public static SingletonProduct createSingleton(SingletonBean bean) {
        return bean;
    }

    @Provides
    @SessionScoped
    public static SessionProduct createSession(SessionBean bean) {
        return bean;
    }

}
