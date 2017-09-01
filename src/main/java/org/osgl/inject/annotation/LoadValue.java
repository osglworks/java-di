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

import org.osgl.inject.ValueLoader;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Used to tag an annotation with {@link ValueLoader value loader}
 * specification. Annotations tagged with `LoadValue` is used to mark
 * a bean instance shall be loaded by value loader instead of being
 * constructed by Genie
 */
@Documented
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ANNOTATION_TYPE, FIELD, PARAMETER})
public @interface LoadValue {
    /**
     * Specifies a {@link ValueLoader} implementation class
     * @return the value loader class
     */
    Class<? extends ValueLoader> value();
}
