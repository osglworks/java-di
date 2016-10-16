package org.osgl.inject;

import org.osgl.$;
import org.osgl.inject.annotation.LoadValue;
import org.osgl.util.E;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
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
        Map<String, Object> options = $.evaluate(anno);
        Class<? extends Annotation> annoType = anno.annotationType();
        LoadValue loadValue;
        if (LoadValue.class == annoType) {
            loadValue = (LoadValue) anno;
        } else {
            loadValue = annoType.getAnnotation(LoadValue.class);
        }
        ValueLoader<T> valueLoader = genie.get(loadValue.value());
        valueLoader.init(options, spec);
        return valueLoader;
    }

}
