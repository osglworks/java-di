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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Note this annotation class is borrowed from cdi-api.
 *
 * <p>
 * Excludes a member of an annotation type (such as a {@link javax.inject.Qualifier qualifier type} or
 * {@link javax.interceptor interceptor binding type}) from consideration when the container compares two annotation
 * instances.
 * </p>
 *
 * <pre>
 * &#064;Qualifier
 * &#064;Retention(RUNTIME)
 * &#064;Target({ METHOD, FIELD, PARAMETER, TYPE })
 * public @interface PayBy {
 *     PaymentMethod value();
 *
 *     &#064;Nonbinding
 *     String comment();
 * }
 * </pre>
 *
 * @author Gavin King
 *
 * @see javax.inject.Qualifier &#064;Qualifier
 * @see javax.interceptor.InterceptorBinding &#064;InterceptorBinding
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Nonbinding {
}
