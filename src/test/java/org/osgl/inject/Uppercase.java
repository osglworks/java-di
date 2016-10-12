package org.osgl.inject;

import org.osgl.inject.annotation.InjectTag;
import org.osgl.inject.annotation.LoadValue;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@InjectTag
@LoadValue(Transformers.ToUpperCase.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Uppercase {
    String value() default "";
}
