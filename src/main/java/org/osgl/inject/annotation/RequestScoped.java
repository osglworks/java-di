package org.osgl.inject.annotation;

import javax.inject.Scope;
import java.lang.annotation.*;

/**
 * Mark a class whose instance, when get injected into program, should be
 * instantiated only once per user request
 *
 * Note we make it apply to {@link ElementType#PARAMETER} by intention so
 * in a controller method we can specify a class (e.g. a collection) as
 * session scoped and framework can keep build up the bean across multiple
 * requests in the same session. Instead of replacing the bean instance
 * everytime with each new request
 *
 * @see Scope
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface RequestScoped {
}
