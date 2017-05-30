package org.osgl.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.*;

/**
 * Mark a Class is stateless in the context of ActFramework.
 *
 * Note this annotation **is** inherited
 *
 * See https://github.com/actframework/actframework/issues/161
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface InheritedStateless {
}
