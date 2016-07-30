package org.osgl.inject.annotation;

import org.osgl.inject.ValueLoader;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link ValueLoader value loader}
 * specification. Annotations tagged with `LoadValue` is used to mark
 * a bean instance shall be loaded by value loader instead of being
 * constructed by Genie
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface LoadValue {
    /**
     * Specifies a {@link ValueLoader} implementation class
     * @return the value loader class
     */
    Class<? extends ValueLoader> value();
}
