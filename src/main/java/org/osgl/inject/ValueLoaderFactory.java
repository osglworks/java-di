package org.osgl.inject;

import org.osgl.inject.annotation.LoadValue;
import org.osgl.util.E;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * `ValueLoaderFactory` load the bean instance directly from
 * {@link ValueLoader} based on the option data specified in
 * {@link org.osgl.inject.annotation.LoadValue} annotation
 */
class ValueLoaderFactory {

    static <T> Provider<T> create(BeanSpec spec, Genie genie) {
        Annotation anno = spec.valueLoader();
        E.illegalArgumentIf(null == anno);
        Map<String, Object> options = new HashMap<String, Object>();
        ElementLoaderProvider.evaluate(anno, options);
        LoadValue loadValue = anno.annotationType().getAnnotation(LoadValue.class);
        ValueLoader<T> valueLoader = genie.get(loadValue.value());
        valueLoader.init(options, spec);
        return valueLoader;
    }

}
