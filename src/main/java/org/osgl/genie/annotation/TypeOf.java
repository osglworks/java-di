package org.osgl.genie.annotation;


import org.osgl.genie.loader.TypedBeanLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark elements of a field or method parameter should be
 * type of specified base class or interface
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Loader(TypedBeanLoader.class)
public @interface TypeOf {
    /**
     * Specify the base class or interface
     *
     * @return the base class or interface
     */
    Class<?> value();
}