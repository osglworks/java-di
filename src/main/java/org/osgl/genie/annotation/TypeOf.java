package org.osgl.genie.annotation;


import org.osgl.genie.loader.TypedElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark elements of a field or method parameter should be
 * type of specified base class or interface.
 *
 * @see Loader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Loader(TypedElementLoader.class)
public @interface TypeOf {
    /**
     * Specify the base class or interface.
     * <p>
     * Default value is {@link TypeOf.PlaceHolder}. When default value
     * is used it directs the genie to use element' type
     * parameter
     *
     * @return the base class or interface
     */
    Class<?> value() default PlaceHolder.class;

    public static final class PlaceHolder {
    }
}