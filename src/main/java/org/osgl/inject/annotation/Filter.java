package org.osgl.inject.annotation;

import org.osgl.inject.ElementFilter;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ElementFilter bean filter}
 * specification.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Filter {
    /**
     * Specify the {@link ElementFilter} implementation used to
     * filter bean(s) loaded by {@link org.osgl.inject.ElementLoader bean loaders}
     *
     * @return the `ElementFilter` class
     */
    Class<? extends ElementFilter> value();
}
