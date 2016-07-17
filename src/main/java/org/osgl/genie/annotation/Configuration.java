package org.osgl.genie.annotation;


import org.osgl.genie.loader.ConfigurationLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark elements of a field or method parameter should be injected
 * from the value provisioned in an application's configuration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Loader(ConfigurationLoader.class)
public @interface Configuration {
    /**
     * Specifies configuration key
     *
     * @return the configuration key
     */
    String value();
}