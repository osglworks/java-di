package org.osgl.genie.builder;

import org.osgl.genie.Builder;
import org.osgl.genie.loader.BeanLoadHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * A builder that relies on a {@link act.di.BeanLoader} to initialize a single
 * object instance
 */
public class BeanBuilder<T> extends Builder<T> {

    protected final BeanLoadHelper helper;

    public BeanBuilder(Class<T> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
        helper = new BeanLoadHelper(annotations, typeParameters);
    }

    @Override
    protected T createInstance() {
        return (T) helper.loadOne();
    }

    @Override
    protected void initializeInstance(T instance) {
        // nothing need to done here
    }
}
