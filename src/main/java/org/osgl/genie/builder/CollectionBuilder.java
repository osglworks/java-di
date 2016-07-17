package org.osgl.genie.builder;

import org.osgl.genie.Builder;
import org.osgl.genie.loader.BeanLoadHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Support injection some common {@link java.util.Collection collections}
 */
public abstract class CollectionBuilder<T extends Collection> extends Builder<T> {

    private BeanLoadHelper helper;

    public CollectionBuilder(Class<? extends T> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
        helper = new BeanLoadHelper(annotations, typeParameters);
    }

    @Override
    protected void initializeInstance(T instance) {
        for (Object o : helper.load()) {
            instance.add(o);
        }
    }
}
