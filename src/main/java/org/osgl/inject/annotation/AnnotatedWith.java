package org.osgl.inject.annotation;


import org.osgl.inject.loader.AnnotatedElementLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark the type of elements of a field or method parameter should
 * be annotated with specified annotation
 */
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@LoadCollection(AnnotatedElementLoader.class)
public @interface AnnotatedWith {
    /**
     * Specify the annotation class
     *
     * @return the annotation class
     */
    Class<?> value();

    /**
     * Specify the type of element the loader should return
     * @return the element type
     */
    org.osgl.inject.ElementType elementType() default org.osgl.inject.ElementType.BEAN;

    /**
     * Should the loader load non-public class or not
     * @return `true` or `false` as described above
     */
    boolean loadNonPublic() default false;

    /**
     * Should the loader load abstract class or not
     *
     * **Note** the value of `loadAbstract` will be ignored if
     * {@link #elementType()} is set to {@link org.osgl.inject.ElementType#BEAN}
     *
     * @return `true` or `false` as described above.
     */
    boolean loadAbstract() default false;

}