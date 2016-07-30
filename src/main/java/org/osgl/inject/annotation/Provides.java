package org.osgl.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a factory method of a module (any class) that can be used to
 * create bean instance. The factory method could be annotated with
 * {@link javax.inject.Qualifier} annotations like {@link javax.inject.Named} to provide
 * some differentiation to injection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Provides {
}
