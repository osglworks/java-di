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
     * Returns parameter value array for a given method.
     *
     * A typical scenario of using this function is when a MVC framework
     * needs to invoke a controller method, it can delegate the
     * param resolving job to the injector by providing a context
     * param provider lookup as described in the param spec. In case
     * the framework needs to invoke the same method in a different context,
     * e.g. in a CLI session, a class typed parameter named context is
     * passed in.
     *
     * The injector shall only build the providers for the first time, and the
     * following calls to this function shall use the cached providers to
     * improve speed.
     *
     * Note not all params is loaded by providers get from the context param
     * provider lookup:
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
     * @param ctxParamProviderLookup a function that returns a provider for a given {@link BeanSpec}
     * @param context identify the current context
     * @return the param value array to feed into the method
     */
    Object[] getParams(Method method, $.Func2<BeanSpec, Injector, Provider> ctxParamProviderLookup, Class context);

}
