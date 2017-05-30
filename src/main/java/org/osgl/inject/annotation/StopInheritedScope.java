package org.osgl.inject.annotation;

import javax.inject.Scope;
import java.lang.annotation.*;

/**
 * Stop a scope specification inherited from super class
 *
 * @see Scope
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StopInheritedScope {
}