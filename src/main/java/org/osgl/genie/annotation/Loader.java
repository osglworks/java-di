package org.osgl.genie.annotation;

import org.osgl.genie.BeanLoader;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link BeanLoader bean loader}
 * specification.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Loader {
    /**
     * Specify the {@link BeanLoader} implementation used to
     * load bean(s)
     * @return the `BeanLoader` implementation
     */
    Class<? extends BeanLoader> value();
}
