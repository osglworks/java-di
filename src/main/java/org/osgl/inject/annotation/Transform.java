package org.osgl.inject.annotation;

import org.osgl.inject.BeanTransformer;

import java.lang.annotation.*;

/**
 * Used to tag an annotation with {@link org.osgl.Osgl.Function}
 * specification.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Transform {
    /**
     * Specify the {@link BeanTransformer bean transformer} implementation
     */
    Class<? extends BeanTransformer> value();
}
