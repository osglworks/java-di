package org.osgl.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a Class is stateful in the context of ActFramework.
 *
 * This annotation is used to terminate the stateless declaration introduced by
 * parent class's {@link InheritedStateless} annotation
 *
 * Note this annotation is **NOT** inherited
 *
 * See https://github.com/actframework/actframework/issues/223
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Stateful {
}
