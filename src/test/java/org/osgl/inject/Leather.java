package org.osgl.inject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RUNTIME)
@javax.inject.Qualifier
public @interface Leather {
    Color color() default Color.TAN;
    public enum Color { RED, BLACK, TAN }
}