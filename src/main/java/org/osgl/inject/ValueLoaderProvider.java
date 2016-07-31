package org.osgl.inject;

import org.osgl.inject.annotation.LoadValue;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * `ValueLoaderProvider` load the bean instance directly from
 * {@link ValueLoader} based on the option data specified in
 * {@link org.osgl.inject.annotation.LoadValue} annotation
 */
class ValueLoaderProvider<T> implements Provider<T> {

    private ValueLoader<T> valueLoader;
    private Map<String, Object> options = C.newMap();
    private BeanSpec spec;

    ValueLoaderProvider(BeanSpec beanSpec, Genie genie) {
        this.spec = beanSpec;
        Annotation anno = beanSpec.valueLoader();
        ElementLoaderProvider.evaluate(anno, options);
        LoadValue loadValue = anno.annotationType().getAnnotation(LoadValue.class);
        valueLoader = genie.get(loadValue.value());
    }

    @Override
    public T get() {
        return valueLoader.load(options, spec);
    }

    static <T> Provider<T> create(BeanSpec spec, Genie genie) {
        Annotation anno = spec.valueLoader();
        E.illegalArgumentIf(null == anno);
        return new ValueLoaderProvider<T>(spec, genie);
    }

}
