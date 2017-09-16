package org.osgl.inject.util;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2017 OSGL (Open Source General Library)
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

import org.junit.Test;
import org.osgl.ut.TestBase;

import java.lang.annotation.*;

public class AnnotationUtilTest extends TestBase {

    @Inherited
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @XyzTag
    @interface AbcTag {}

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface XyzTag {}

    @AbcTag
    @XyzTag
    static class Parent {}

    static class Child extends Parent {}

    @Test
    public void itShallReturnDeclaredAnnotations() {
        notNull(AnnotationUtil.declaredAnnotation(Parent.class, AbcTag.class));
    }

    @Test
    public void itShallNotReturnInheritedAnnotations() {
        isNull(AnnotationUtil.declaredAnnotation(Child.class, AbcTag.class));
    }

    @Test
    public void tagAnnotationShallReturnTagAnnotationIfPresented() {
        AbcTag abcTag = AnnotationUtil.declaredAnnotation(Parent.class, AbcTag.class);
        XyzTag xyzTag = AnnotationUtil.tagAnnotation(abcTag, XyzTag.class);
        notNull(xyzTag);
    }

    @Test
    public void tagAnnotationShallReturnNullIfAnnotationTagNotPresented() {
        XyzTag xyzTag = Child.class.getAnnotation(XyzTag.class);
        notNull(xyzTag);
        AbcTag abcTag = AnnotationUtil.tagAnnotation(xyzTag, AbcTag.class);
        isNull(abcTag);
    }

    @Test
    public void testCreateAnnotation() {
        XyzTag xyzTag = AnnotationUtil.createAnnotation(XyzTag.class);
        notNull(xyzTag);
        eq(Child.class.getAnnotation(XyzTag.class), xyzTag);
    }

}
