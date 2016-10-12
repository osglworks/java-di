package org.osgl.inject.annotation;

import java.lang.annotation.*;

/**
 * Used to specify a certain annotation is an `Inject tag`
 *
 * @see org.osgl.inject.Genie#registerInjectTag(Class[])
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InjectTag {
}
