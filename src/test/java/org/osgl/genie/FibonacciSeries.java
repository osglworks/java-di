package org.osgl.genie;

import org.osgl.genie.annotation.Loader;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Loader(FibonacciSeriesLoader.class)
public @interface FibonacciSeries {
    /**
     * The max value in the series
     * @return the max value
     */
    int max() default 100;
}
