package org.osgl.genie;

import org.osgl.$;
import org.osgl.genie.annotation.Filter;
import org.osgl.genie.annotation.Loader;
import org.osgl.genie.annotation.MapKey;
import org.osgl.util.C;

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
        final ElementFilter filter;
        final Map<String, Object> options = C.newMap();
        final $.Function<Object, Boolean> predicate;
        final BeanSpec containerSpec;

        FilterInfo(ElementFilter filter, Annotation anno, Class<? extends Annotation> annoClass, BeanSpec container) {
            this.filter = filter;
            Method[] ma = annoClass.getMethods();
            for (Method m : ma) {
                if (isStandardAnnotationMethod(m)) {
                    continue;
                }
                options.put(m.getName(), $.invokeVirtual(anno, m));
            }
            predicate = filter.filter(options, container);
            this.containerSpec = container;
        }

        public boolean test(Object bean) {
            return predicate.apply(bean);
        }
    }

    private static class LoaderInfo extends FilterInfo implements Comparable<LoaderInfo> {

        LoaderInfo(ElementLoader loader, Annotation anno, Class<? extends Annotation> annoClass, BeanSpec container) {
            super(loader, anno, annoClass, container);
        }

        ElementLoader loader() {
            return (ElementLoader) filter;
        }

        @Override
        public int compareTo(LoaderInfo o) {
            return loader().priority() - o.loader().priority();
        }

        Iterable load(Genie genie) {
            return loader().load(options, containerSpec, genie);
        }
    }

    private static class CollectionLoaderProvider<T extends Collection> extends ElementLoaderProvider<T> {
        CollectionLoaderProvider(BeanSpec spec, Provider<T> provider, Genie genie) {
            super(spec, provider, genie);
        }

        @Override
        protected void populate(T bean, Object element) {
            bean.add(element);
        }
    }

    private static class MapLoaderProvider<T extends Map> extends ElementLoaderProvider<T> {

        String hint;
        KeyExtractor keyExtractor;

        MapLoaderProvider(BeanSpec spec, Provider<T> provider, Genie genie) {
            super(spec, provider, genie);
            MapKey mapKey = spec.mapKey();
            this.keyExtractor = genie.get(mapKey.extractor());
            this.hint = mapKey.value();
        }

        @Override
        protected void populate(T bean, Object element) {
            bean.put(keyExtractor.keyOf(hint, element), element);
        }
    }


    private final Provider<T> realProvider;
    private final LoaderInfo loader;
    private final Set<FilterInfo> filters;
    private final Genie genie;

    private ElementLoaderProvider(BeanSpec spec, Provider<T> provider, Genie genie) {
        this.realProvider = provider;
        C.List<LoaderInfo> loaders = loaders(genie, spec).sorted();
        this.loader = loaders.first();
        List<? extends FilterInfo> tail = loaders.head(-1);
        this.filters = C.set(filters(genie, spec).append(tail));
        this.genie = genie;
    }

    @Override
    public final T get() {
        T bean = realProvider.get();
        $.Predicate predicate = $.F.and(filters.toArray(new FilterInfo[filters.size()]));
        for (Object element : loader.load(genie)) {
            if (predicate.test(element)) {
                populate(bean, element);
            }
        }
        return bean;
    }

    protected abstract void populate(T bean, Object element);

    static <T> Provider<T> decorate(BeanSpec spec, Provider<T> provider, Genie genie) {
        if (!spec.hasLoader()) {
            return provider;
        }

        if (provider instanceof ElementLoaderProvider) {
            return $.cast(provider);
        }

        if (spec.isMap()) {
            return new MapLoaderProvider(spec, provider, genie);
        }

        return new CollectionLoaderProvider(spec, provider, genie);
    }

    private static C.List<LoaderInfo> loaders(Genie genie, BeanSpec spec) {
        C.List<LoaderInfo> list = C.newList();
        Set<Annotation> loaders = spec.loaders();
        for (Annotation anno : loaders) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            Loader loaderTag = annoClass.getAnnotation(Loader.class);
            ElementLoader loader = genie.get(loaderTag.value());
            list.add(new LoaderInfo(loader, anno, annoClass, spec));
        }
        return list;
    }

    private static C.List<FilterInfo> filters(Genie genie, BeanSpec spec) {
        C.List<FilterInfo> list = C.newList();
        Set<Annotation> annotations = spec.filters();
        for (Annotation anno : annotations) {
            Class<? extends Annotation> annoClass = anno.annotationType();
            Filter filterTag = annoClass.getAnnotation(Filter.class);
            ElementFilter loader = genie.get(filterTag.value());
            list.add(new FilterInfo(loader, anno, annoClass, spec));
        }
        return list;
    }

    private static Set<String> standards = C.newSet(C.list("equals", "hashCode", "toString", "annotationType", "getClass"));

    private static boolean isStandardAnnotationMethod(Method m) {
        return standards.contains(m.getName());
    }
}

