package org.osgl.inject.annotation;

import javax.inject.Scope;
import java.lang.annotation.*;

/**
 * Mark a class whose instance, when get injected into program, should be
 * instantiated only once per user session
 *
 * Note we make it apply to {@link ElementType#PARAMETER} by intention so
 * in a controller method we can specify a class (e.g. a collection) to indicate
 * the bean shall persist within a single request scope. E.g. if the interceptor
 * and the action handler has the same signature, the bean will NOT been
 * constructed for multiple times
 *
 * @see Scope
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface SessionScoped {
}