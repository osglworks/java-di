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

import com.google.inject.internal.cglib.core.$DefaultGeneratorStrategy;
import org.osgl.inject.KeyExtractor;
import org.osgl.inject.util.AnnotationUtil;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.lang.annotation.*;
import java.util.Map;

/**
 * Used to specify how to extract {@link java.util.Map} key
 * from a value.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface MapKey {

    /**
     * Specify the `hint` to be passed into {@link KeyExtractor#keyOf(String, Object)}
     * function call.
     *
     * @return the `hint` used to extract the key. Default value is ""
     */
    String value() default "";

    /**
     * Specify a {@link KeyExtractor key extractor}. Default value is
     * {@link org.osgl.inject.KeyExtractor.PropertyExtractor}.
     */
    Class<? extends KeyExtractor> extractor() default KeyExtractor.PropertyExtractor.class;

    class Factory {
        public static MapKey create(String value) {
            E.illegalArgumentIf(S.isBlank(value), "Value required");
            Map<String, Object> memberValues = C.newMap("value", value);
            return AnnotationUtil.createAnnotation(MapKey.class, memberValues);
        }
    }
}
