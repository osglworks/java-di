package org.osgl.inject.annotation;

import java.lang.annotation.*;

/**
 * Used to mark a parameter should be injected from {@link org.osgl.inject.Injector}.
 *
 * The whole reason for this annotation to be exists is because {@link javax.inject.Inject}
 * annotation does not apply to parameters
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Provided {
}
