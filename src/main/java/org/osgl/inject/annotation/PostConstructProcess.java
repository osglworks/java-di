package org.osgl.inject.annotation;

import org.osgl.inject.PostConstructProcessor;

import java.lang.annotation.*;

/**
 * When a field or parameter is annotated with a `PostConstructProcess`
 * tagged annotation, it tells Genie to load specified {@link org.osgl.inject.PostConstructProcessor} to apply on the bean
 * after bean is constructed and, if the bean has {@link javax.annotation.PostConstruct}
 * method, after that method is called
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface PostConstructProcess {
    Class<? extends PostConstructProcessor> value();
}
