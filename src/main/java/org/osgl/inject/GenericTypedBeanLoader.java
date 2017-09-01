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

/**
 * A `GenericTypedBeanLoader` can be used to load instance of certain type
 * with generic type parameters.
 *
 * A typical usage scenario is to load a `Dao` implementation. E.g.
 *
 * ```
 * public class FooService {
 *     {@literal @}Inject
 *      private Dao<Foo> fooDao;
 *      ...
 * }
 * ```
 *
 * One must register the `GenericTypedBeanLoader` via calling
 * the {@link Genie#registerGenericTypedBeanLoader(Class, GenericTypedBeanLoader)}
 * method
 */
public interface GenericTypedBeanLoader<T> {
    /**
     * Returns an instance matches the spec
     * @param spec the bean spec
     * @return the bean instance
     */
    T load(BeanSpec spec);
}
