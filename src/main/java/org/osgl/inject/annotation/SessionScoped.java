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

import javax.inject.Scope;
import java.lang.annotation.*;

/**
 * Mark a class whose instance, when get injected into program, should be
 * instantiated only once per user session
 *
 * Note we make it apply to {@link ElementType#PARAMETER} by intention so
 * in a controller method we can specify a class (e.g. a collection) to indicate
 * the bean shall persist within a single request scope. E.g. if the interceptor
 * and the action handler has the same signature, the bean will NOT been
 * constructed for multiple times
 *
 * @see Scope
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface SessionScoped {
}
