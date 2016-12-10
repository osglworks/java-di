package org.osgl.inject.annotation;

import org.osgl.inject.loader.ConfigurationValueLoader;

import java.lang.annotation.*;

/**
 * Used to specify a field or parameter shall be load by
 * {@link org.osgl.inject.loader.ConfigurationValueLoader}
 */
@Documented
@InjectTag
@LoadValue(ConfigurationValueLoader.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Configuration {

    /**
     * Specify the configuration key
     *
     * @return the configuration key
     */
    String value();
}
