package org.osgl.genie.annotation;

import org.osgl.genie.ElementFilter;

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
     * filter bean(s) loaded by {@link org.osgl.genie.ElementLoader bean loaders}
     *
     * @return the `ElementFilter` class
     */
    Class<? extends ElementFilter> value();
}
