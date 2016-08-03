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
     * Returns parameter array for a given method.
     *
     * A typical scenario of using this function is when a MVC framework
     * needs to invoke a controller action handler, it can delegate the
     * param preparing job to the injector by providing a generic
     * {@link ValueLoader value loader}, which load value from some
     * contextual data source, e.g. the request params or header etc.
     *
     * Note not all params is loaded by the default value loader. Belows
     * are the exceptions:
     *
     * The params annotated with {@link org.osgl.inject.annotation.Provided}
     * shall not be load using the default value loader, instead it needs to
     * go normal provider finding process. Those params are generally contextual
     * dependencies, e.g. a `DAO` to support database access
     *
     * The params annotated with any {@link org.osgl.inject.annotation.LoadValue}
     * tagged annotation shall not load using default value loader, instead it
     * shall load using the annotated value loader. This is usually for getting
     * configuration information
     *
     * The params annotated with any {@link org.osgl.inject.annotation.LoadCollection}
     * tagged annotation shall not load using default value loader, instead it
     * shall construct the collection and use the specified {@link ElementLoader}
     * to load the elements
     *
     * @param method the method
     * @param defaultValueLoader provides value when param is not annotated
     *                           with {@link org.osgl.inject.annotation.Provided}
     *                           and neither {@link ValueLoader} is specified nor
     *                           {@link ElementLoader} is specified
     * @return the parameters that an be used to invoke the method
     */
    Object[] getParams(Method method, ValueLoader<?> defaultValueLoader);

}
