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
 * Mark a method parameter value should be provided by
 * {@link org.osgl.inject.Injector}.
 *
 * This annotation is created because {@link javax.inject.Inject}
 * annotation cannot be applied on method parameters
 *
 * **Note** Genie does not know how to inject method parameters
 * and this annotation is provided to support framework author
 * on implementing method parameter injection. For example
 * [ActFramework](http://www.actframework.org) favor this annotation
 * on controller action handler parameter injection implementation
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Provided {
}
