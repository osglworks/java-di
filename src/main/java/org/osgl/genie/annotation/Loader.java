package org.osgl.genie.annotation;

import org.osgl.genie.ElementLoader;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ElementLoader bean loader}
 * specification.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Loader {
    /**
     * Specify the {@link ElementLoader} implementation used to
     * load bean(s)
     * @return the `ElementLoader` implementation
     */
    Class<? extends ElementLoader> value();
}
