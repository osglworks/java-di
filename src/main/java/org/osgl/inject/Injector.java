package org.osgl.inject;

import org.osgl.$;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A generic Injector interface defines the contract
 * a dependency injector like {@link Genie} should provide
 */
public interface Injector {
    /**
     * Returns a bean of given type
     *
     * @param type the class of the bean
     * @param <T>  generic type of the bean
     * @return the bean
     */
    <T> T get(Class<T> type);

    /**
     * Check if a supplied annotation class is a {@link javax.inject.Qualifier}
     * @param annoClass
     * @return `true` if the annotation type is a qualifier or `false` otherwise
     */
    boolean isQualifier(Class<? extends Annotation> annoClass);

    /**
     * Check if a supplied annotation class specifies a {@link PostConstructProcessor}
     * @param annoClass the annotation type
     * @return `true` if the annotation type specifies post construct processor
     */
    boolean isPostConstructProcessor(Class<? extends Annotation> annoClass);

    /**
     * Check if a supplied annotation class is a {@link javax.inject.Scope} annotation
     * @param annoClass the annotation type
     * @return `true` if the annotation type indicate a scope
     */
    boolean isScope(Class<? extends Annotation> annoClass);
}
