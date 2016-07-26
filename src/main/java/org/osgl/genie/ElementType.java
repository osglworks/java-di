package org.osgl.genie;

import org.osgl.$;
import org.osgl.util.C;

import java.util.List;

/**
 * Used in {@link org.osgl.genie.annotation.AnnotatedWith} and
 * {@link org.osgl.genie.annotation.TypeOf} annotation to specify
 * the type of the element should be returned by
 * {@link org.osgl.genie.loader.TypedElementLoader} and
 * {@link org.osgl.genie.loader.AnnotatedElementLoader} respectively
 */
public enum ElementType {
    /**
     * Specify the element loader shall return the Class found
     */
    CLASS () {
        @Override
        public List<Object> transform(List<Class<?>> classes, Genie genie) {
            return (List)classes;
        }
    },

    /**
     * Specify the element loader shall return the bean instantiated
     */
    BEAN () {
        @Override
        public List<Object> transform(List<Class<?>> classes, final Genie genie) {
            return C.list(classes).map(new $.Transformer<Class, Object>() {
                @Override
                public Object transform(Class aClass) {
                    return genie.get(aClass);
                }
            });
        }
    };

    public abstract List<Object> transform(List<Class<?>> classes, Genie genie);

    public boolean loadAbstract() {
        return this != BEAN;
    }
}
