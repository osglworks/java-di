package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.BeanFilter;
import org.osgl.genie.BeanLoader;
import org.osgl.genie.Genie;
import org.osgl.genie.InjectException;
import org.osgl.genie.annotation.Filter;
import org.osgl.genie.annotation.Loader;
import org.osgl.util.C;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A helper class to provide a list of bean instance based on
 * container's type parameter and annotation information.
 *
 * If there is no annotation, then it will check the container's
 * type parameter and use it as base class or interface to find
 * all implementations and load them as bean instances
 */
public class BeanLoadHelper {

    private class FilterInfo extends $.Predicate {
        BeanFilter filter;
        Object hint;
        Map<String, Object> options = C.newMap();
        $.Function<Object, Boolean> predicate;
        FilterInfo(BeanFilter filter, Annotation anno, Class<? extends Annotation> annoClass) {
            this.filter = filter;
            Method[] ma = annoClass.getMethods();
            for (Method m: ma) {
                if (isStandardAnnotationMethod(m)) {
                    continue;
                }
                if ("value".equals(m.getName())) {
                    hint = $.invokeVirtual(anno, m);
                } else {
                    options.put(m.getName(), $.invokeVirtual(anno, m));
                }
            }
            predicate = filter.filter(hint, options);
        }
        public boolean test(Object bean) {
            return predicate.apply(bean);
        }
    }

    private class LoaderInfo extends FilterInfo implements Comparable<LoaderInfo> {

        LoaderInfo(BeanLoader loader, Annotation anno, Class<? extends Annotation> annoClass) {
            super(loader, anno, annoClass);
        }

        BeanLoader loader() {
            return (BeanLoader) filter;
        }

        @Override
        public int compareTo(LoaderInfo o) {
            return loader().compareTo(o.loader());
        }

        List loadMultiple() {
            return loader().loadMultiple(hint, options);
        }

    }

    private C.List<LoaderInfo> loaders = C.newList();
    private C.List<FilterInfo> filters = C.newList();

    public BeanLoadHelper(Set<Annotation> annotations, List<Type> typeParameters) {
        this.resolveAnnotations(annotations);
        if (loaders.isEmpty()) {
            this.resolveTypeParameters(typeParameters);
        }
    }

    public List load() {
        if (loaders.isEmpty()) {
            throw new InjectException("Load not found");
        }
        List<LoaderInfo> loaderFilters = loaders.head(-1);
        C.List<FilterInfo> appendedFilters = filters.append(loaderFilters);
        Osgl.Predicate predicate = $.F.and(appendedFilters.toArray(new FilterInfo[appendedFilters.size()]));
        LoaderInfo loader = loaders.first();
        return C.list(loader.loadMultiple()).filter(predicate);
    }

    public Object loadOne() {
        List list = load();
        return list.isEmpty() ? null : list.get(0);
    }

    private void resolveAnnotations(Set<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return;
        }
        Genie genie = Genie.get();
        for (Annotation anno : annotations) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            Loader loaderTag = annoClass.getAnnotation(Loader.class);
            if (null != loaderTag) {
                BeanLoader beanLoader = genie.get(loaderTag.value());
                loaders.add(new LoaderInfo(beanLoader, anno, annoClass));
                continue;
            }
            Filter filterTag = annoClass.getAnnotation(Filter.class);
            if (null != filterTag) {
                BeanFilter beanFilter = genie.get(filterTag.value());
                filters.add(new FilterInfo(beanFilter, anno, annoClass));
            }
        }
        Collections.sort(loaders);
    }

    private void resolveTypeParameters(List<Type> typeParameters) {
        if (typeParameters.isEmpty()) {
            return;
        }
        // always choose the last one in the array as there are two possibilities:
        // 1. Collection type: there is only one element in the array
        // 2. Map: the second one is the value type
        Type effectiveType = typeParameters.get(typeParameters.size() - 1);
        if (effectiveType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) effectiveType;
        }
    }

    private static Set<String> standards = C.newSet(C.list("equals", "hashCode", "toString", "annotationType"));

    private boolean isStandardAnnotationMethod(Method m) {
        return standards.contains(m.getName());
    }
}
