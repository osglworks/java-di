package org.osgl.inject;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A generic Injector interface defines the contract
 * {@link Genie} provides
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
     * Returns a bean of given type and annotations. This is helpful
     * when it needs to inject a value for a method parameter
     *
     * @param type        the type of the bean
     * @param annotations the annotations tagged to the (parameter)
     * @param <T>         the generic type
     * @return the bean instance
     */
    <T> T get(Type type, Annotation[] annotations);

    /**
     * Returns parameter array for a given method
     *
     * @param method the method
     * @param defaultValueLoader provides value when param is not annotated
     *                           with {@link org.osgl.inject.annotation.Provided}
     *                           and neither {@link ValueLoader} is specified nor
     *                           {@link ElementLoader} is specified
     * @return the parameters that an be used to invoke the method
     */
    Object[] getParams(Method method, ValueLoader<?> defaultValueLoader);

    <T> void registerProvider(Class<T> type, Provider<? extends T> provider);

    void registerScopeAlias(Class<? extends Annotation> scopeAnnotation, Class<? extends Annotation> scopeAlias);

    void registerScopeProvider(Class<? extends Annotation> scopeAnnotation, ScopeCache scopeCache);

    void registerScopeProvider(Class<? extends Annotation> scopeAnnotation, Class<? extends ScopeCache> scopeCacheClass);

    void registerPostConstructProcessor(
            Class<? extends Annotation> annoClass,
            PostConstructProcessor<?> processor
    );
}
