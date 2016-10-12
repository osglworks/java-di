package org.osgl.inject;

import org.osgl.inject.annotation.InjectTag;
import org.osgl.inject.annotation.LoadValue;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@InjectTag
@LoadValue(Transformers.ToLowerCase.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lowercase {
    String value() default "";
}
