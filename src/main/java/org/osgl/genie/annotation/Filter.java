package org.osgl.genie.annotation;

import org.osgl.genie.BeanFilter;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link BeanFilter bean filter}
 * specification.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Filter {
    /**
     * Specify the {@link BeanFilter} implementation used to
     * filter bean(s) loaded by {@link org.osgl.genie.BeanLoader bean loaders}
     * @return the `BeanFilter` class
     */
    Class<? extends BeanFilter> value();
}
