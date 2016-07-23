package org.osgl.genie.annotation;

import org.osgl.genie.loader.ConfigurationValueLoader;

import java.lang.annotation.*;

/**
 * Used to specify a field or parameter shall be load by
 * {@link org.osgl.genie.loader.ConfigurationValueLoader}
 */
@Documented
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
