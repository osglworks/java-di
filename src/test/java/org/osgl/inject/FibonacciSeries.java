package org.osgl.inject;

import org.osgl.inject.annotation.LoadCollection;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@LoadCollection(FibonacciSeriesLoader.class)
public @interface FibonacciSeries {
    /**
     * The max value in the series
     * @return the max value
     */
    int max() default 100;
}
