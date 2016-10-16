package org.osgl.inject.annotation;

import org.osgl.inject.BeanSpec;
import org.osgl.inject.ElementLoader;

import java.lang.annotation.*;
import java.util.Map;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Used to tag an annotation with {@link ElementLoader collection element loader}
 * specification. Annotations tagged with `LoadCollection` is used to mark
 * a {@link java.util.Collection} or {@link java.util.Map} type
 * inject target needs additional logic to load element data
 */
@Documented
@InjectTag
@Retention(RetentionPolicy.RUNTIME)
@Target({ANNOTATION_TYPE, FIELD, PARAMETER})
public @interface LoadCollection {
    /**
     * Specify the {@link ElementLoader} implementation used to
     * load bean(s)
     *
     * @return the `ElementLoader` implementation
     */
    Class<? extends ElementLoader> value();

    /**
     * Indicate it shall reverse the {@link org.osgl.inject.ElementFilter#filter(Map, BeanSpec) filter function}
     * when loading element from collection
     */
    boolean reverseFilter() default false;
}
