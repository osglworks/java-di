package org.osgl.inject.util;

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
