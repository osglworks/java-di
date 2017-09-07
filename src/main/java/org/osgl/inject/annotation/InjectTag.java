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

import java.lang.annotation.*;

/**
 * Used to specify a certain annotation is an `Inject tag`.
 *
 * Once an annotation is annotated with `InjectTag` it means
 * the field or method is subjected to dependency injection
 * without the need for {@link javax.inject.Inject} annotation.
 *
 * For example, let's say a developer want to create an annotation
 * to inject fibonacci series:
 *
 * ```
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * {@literal @}InjectTag
 * {@literal @}LoadCollection(FibonacciSeriesLoader.class)
 * public @interface FibonacciSeries {
 *     int max() default 100;
 * }
 * ```
 *
 * Because the `FibonacciSeries` is tagged with `InjectTag`, thus
 * user can directly use it to mark a field is subject to
 * dependency injection:
 *
 * ```java
 * public class EvenFibonacciSeriesHolder {
 *     {@literal @}FibonacciSeries List<Integer> series;
 * }
 * ```
 *
 * If the `FibonacciSeries` annotation is not tagged with
 * `InjectTag`, then it must add `@Inject` in order to
 * mark the field needs dependency injection:
 *
 * ```java
 * public class EvenFibonacciSeriesHolder {
 *     {@literal @}Inject @FibonacciSeries List<Integer> series;
 * }
 * ```
 *
 * @see org.osgl.inject.Genie#registerInjectTag(Class[])
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InjectTag {
}

