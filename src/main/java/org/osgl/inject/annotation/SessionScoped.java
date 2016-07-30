package org.osgl.inject.annotation;

import javax.inject.Scope;
import java.lang.annotation.*;

/**
 * Mark a class whose instance, when get injected into program, should be
 * instantiated only once per user session
 *
 * @see Scope
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SessionScoped {
}