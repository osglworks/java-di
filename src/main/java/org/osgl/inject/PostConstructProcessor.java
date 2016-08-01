package org.osgl.inject;

import java.lang.annotation.Annotation;

/**
 * Define the logic that needs to be invoked on the bean before return back
 */
public interface PostConstructProcessor<T> {
    /**
     * Process a `T` typed bean with the relevant annotation instance
     * @param bean the bean to be processed
     * @param annotation the relevant annotation tagged on the parameter or field
     */
    void process(T bean, Annotation annotation);
}
