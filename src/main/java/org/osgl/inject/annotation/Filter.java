package org.osgl.inject.annotation;

import org.osgl.inject.ElementFilter;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ElementFilter bean filter}
 * specification.
 *
 * This annotation can be used in conjunction with {@link LoadCollection}
 * to filter the element to be loaded into a collection typed bean
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Filter {
    /**
     * Specify the {@link ElementFilter} implementation used to
     * filter bean(s) loaded by {@link org.osgl.inject.ElementLoader bean loaders}
     *
     * @return the `ElementFilter` class
     */
    Class<? extends ElementFilter> value();

    /**
     * Specify it shall reverse the filter function when applying the filter
     */
    boolean reverse() default false;
}
