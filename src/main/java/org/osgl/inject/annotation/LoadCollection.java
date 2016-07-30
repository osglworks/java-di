package org.osgl.inject.annotation;

import org.osgl.inject.ElementLoader;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ElementLoader collection element loader}
 * specification. Annotations tagged with `LoadCollection` is used to mark
 * a {@link java.util.Collection} or {@link java.util.Map} type
 * inject target needs additional logic to load element data
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface LoadCollection {
    /**
     * Specify the {@link ElementLoader} implementation used to
     * load bean(s)
     *
     * @return the `ElementLoader` implementation
     */
    Class<? extends ElementLoader> value();
}
