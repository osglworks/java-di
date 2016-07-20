package org.osgl.genie;

import org.osgl.$;
import org.osgl.genie.annotation.Filter;
import org.osgl.genie.annotation.Loader;
import org.osgl.genie.annotation.MapKey;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A provider decorate that add element loading function decorator
 * to underline provider
 */
abstract class ElementLoaderProvider<T> implements Provider<T> {

    private static class FilterInfo extends $.Predicate {
        ElementFilter filter;
        Map<String, Object> options = C.newMap();
        $.Function<Object, Boolean> predicate;

        FilterInfo(ElementFilter filter, Annotation anno, Class<? extends Annotation> annoClass) {
            this.filter = filter;
            Method[] ma = annoClass.getMethods();
            for (Method m: ma) {
                if (isStandardAnnotationMethod(m)) {
                    continue;
                }
                options.put(m.getName(), $.invokeVirtual(anno, m));
            }
            predicate = filter.filter(options);
        }
        public boolean test(Object bean) {
            return predicate.apply(bean);
        }
    }

    private static class LoaderInfo extends FilterInfo implements Comparable<LoaderInfo> {

        LoaderInfo(ElementLoader loader, Annotation anno, Class<? extends Annotation> annoClass) {
            super(loader, anno, annoClass);
        }

        ElementLoader loader() {
            return (ElementLoader) filter;
        }

        @Override
        public int compareTo(LoaderInfo o) {
            return loader().priority() - o.loader().priority();
        }

        Iterable load() {
            return loader().load(options);
        }
    }

    private static class CollectionLoaderProvider<T extends Collection> extends ElementLoaderProvider<T> {
        CollectionLoaderProvider(Genie.Key key, Provider<T> provider, Genie genie) {
            super(key, provider, genie);
        }

        @Override
        protected void populate(T bean, Object element) {
            bean.add(element);
        }
    }

    private static class MapLoaderProvider<T extends Map> extends ElementLoaderProvider<T> {

        String hint;
        KeyExtractor keyExtractor;

        MapLoaderProvider(Genie.Key key, Provider<T> provider, Genie genie) {
            super(key, provider, genie);
            MapKey mapKey = key.mapKey();
            this.keyExtractor = genie.get(mapKey.extractor());
            this.hint = mapKey.value();
        }

        @Override
        protected void populate(T bean, Object element) {
            bean.put(keyExtractor.keyOf(hint, element), element);
        }
    }


    private Provider<T> realProvider;
    private LoaderInfo loader;
    private Set<FilterInfo> filters;

    private ElementLoaderProvider(Genie.Key key, Provider<T> provider, Genie genie) {
        this.realProvider = provider;
        C.List<LoaderInfo> loaders = loaders(genie, key).sorted();
        this.loader = loaders.first();
        List<? extends FilterInfo> tail = loaders.head(-1);
        this.filters = C.set(filters(genie, key).append(tail));
    }

    @Override
    public final T get() {
        T bean = realProvider.get();
        $.Predicate predicate = $.F.and(filters.toArray(new FilterInfo[filters.size()]));
        for (Object element : loader.load()) {
            if (predicate.test(element)) {
                populate(bean, element);
            }
        }
        return bean;
    }

    protected abstract void populate(T bean, Object element);

    static <T> Provider<T> decorate(Genie.Key key, Provider<T> provider, Genie genie) {
        if (!key.hasLoader()) {
            return provider;
        }

        if (provider instanceof ElementLoaderProvider) {
            return $.cast(provider);
        }

        if (key.isMap()) {
            return new MapLoaderProvider(key, provider, genie);
        }

        return new CollectionLoaderProvider(key, provider, genie);
    }

    private static C.List<LoaderInfo> loaders(Genie genie, Genie.Key key) {
        C.List<LoaderInfo> list = C.newList();
        Set<Annotation> loaders = key.loaders();
        for (Annotation anno : loaders) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            Loader loaderTag = annoClass.getAnnotation(Loader.class);
            ElementLoader loader = genie.get(loaderTag.value());
            list.add(new LoaderInfo(loader, anno, annoClass));
        }
        return list;
    }

    private static C.List<FilterInfo> filters(Genie genie, Genie.Key key) {
        C.List<FilterInfo> list = C.newList();
        Set<Annotation> annotations = key.filters();
        for (Annotation anno : annotations) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            Filter filterTag = annoClass.getAnnotation(Filter.class);
            ElementFilter loader = genie.get(filterTag.value());
            list.add(new FilterInfo(loader, anno, annoClass));
        }
        return list;
    }

    private static Set<String> standards = C.newSet(C.list("equals", "hashCode", "toString", "annotationType", "getClass"));

    private static boolean isStandardAnnotationMethod(Method m) {
        return standards.contains(m.getName());
    }
}

