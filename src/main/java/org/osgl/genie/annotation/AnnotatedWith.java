package org.osgl.genie.annotation;


import org.osgl.genie.loader.TypedBeanLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark the type of elements of a field or method parameter should
 * be annotated with specified annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Loader(TypedBeanLoader.class)
public @interface AnnotatedWith {
    /**
     * Specify the annotation class
     *
     * @return the annotation class
     */
    Class<?> value();
}