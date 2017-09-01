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

import org.osgl.inject.PostConstructProcessor;

import java.lang.annotation.*;

/**
 * When a field or parameter is annotated with a `PostConstructProcess`
 * tagged annotation, it tells Genie to load specified {@link org.osgl.inject.PostConstructProcessor} to apply on the bean
 * after bean is constructed and, if the bean has {@link javax.annotation.PostConstruct}
 * method, after that method is called
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface PostConstructProcess {
    Class<? extends PostConstructProcessor> value();
}
