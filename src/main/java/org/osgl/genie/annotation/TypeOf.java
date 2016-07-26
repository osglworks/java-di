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
 * @see LoadCollection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@LoadCollection(TypedElementLoader.class)
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

    /**
     * Specify the type of element the loader should return
     *
     * **Note** when the generic type of element is `Class<...>` the
     * value of elementType will always be treated as {@link org.osgl.genie.ElementType#CLASS}
     * and user setting through this property will be ignored
     *
     * @return the element type
     */
    org.osgl.genie.ElementType elementType() default org.osgl.genie.ElementType.BEAN;


    /**
     * Should the loader load non-public class or not
     * @return `true` or `false` as described above
     */
    boolean loadNonPublic() default false;

    /**
     * Should the loader load abstract class or not
     *
     * **Note** the value of `loadAbstract` will be ignored if
     * {@link #elementType()} is set to {@link org.osgl.genie.ElementType#BEAN}
     *
     * @return `true` or `false` as described above.
     */
    boolean loadAbstract() default false;

    /**
     * Should the loader load Class/Bean using the class specified by {@link #value()}.
     * If specified as `false` then it shall load purely sub type of the type specified, otherwise
     * it might load the specified type if:
     *
     * 1. {@link #elementType()} is {@link org.osgl.genie.ElementType#CLASS}
     * 2. {@link #elementType()} is {@link org.osgl.genie.ElementType#BEAN} and the specified
     *    type is non-static class and can initialized
     * @return `true` or `false` as described above
     */
    boolean loadRoot() default false;
}