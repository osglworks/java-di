package org.osgl.genie;

import org.osgl.$;
import org.osgl.genie.annotation.Filter;
import org.osgl.genie.annotation.Loader;
import org.osgl.genie.annotation.Provides;
import org.osgl.genie.util.AnnotationUtil;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Genie is responsible for providing instance as required
 */
public class Genie implements DependencyInjector {

    public static class Binder<T> {
        private Class<T> type;
        private Provider<? extends T> provider;
        private List<Annotation> annotations = C.newList();
        Binder(Class<T> type) {
            this.type = type;
        }

        public Binder<T> to(final Class<? extends T> impl) {
            this.provider = new Provider<T>() {
                @Override
                public T get() {
                    return Genie.get().get(impl);
                }
            };
            return this;
        }

        public Binder<T> to(Provider<? extends T> provider) {
            this.provider = provider;
            return this;
        }

        public Binder<T> withAnnotation(Class<? extends Annotation> annotation) {
            annotations.add(AnnotationUtil.createAnnotation(annotation));
            return this;
        }

        boolean bound() {
            return null != provider;
        }

        void register(Genie genie) {
            if (!bound()) {
                return;
            }
            genie.registerProvider(key(), provider);
        }

        Key key() {
            return new Key(type, annotations.toArray(new Annotation[annotations.size()]));
        }
    }

    private static class Key {

        /**
         * Used to sort annotation list so we can make equality compare between
         * `Key` generated from {@link Provides provider} factory method
         * and the `Key` generated from field or method parameters.
         *
         * The logic simply relies on annotation's class. As it is clear that the
         * same annotation type cannot be tagged multiple times
         */
        private static Comparator<Annotation> ANNO_CMP = new Comparator<Annotation>() {
            @Override
            public int compare(Annotation o1, Annotation o2) {
                Class c1 = o1.getClass();
                Class c2 = o2.getClass();
                if (c1 != c2) {
                    return c1.getName().compareTo(c2.getName());
                }
                return 0;
            }
        };

        private final int hc;
        private final Type type;
        private final C.List<Annotation> annotations = C.newList();
        private final Set<Annotation> loaders = C.newSet();
        private final Set<Annotation> filters = C.newSet();
        private final Set<Annotation> qualifiers = C.newSet();
        private List<Type> typeParams;

        /**
         * Construct the `Key` with bean type and field or parameter
         * annotations
         * @param type the type of the bean to be instantiated
         * @param annotations the annotation tagged on field or parameter,
         *                    or `null` if this is a direct API injection
         *                    request
         */
        Key(Type type, Annotation[] annotations) {
            this.type = type;
            this.resolve(annotations);
            hc = $.hc(type, this.annotations);
        }

        private Key(Key providerKey) {
            if (!providerKey.isProvider()) {
                throw new IllegalStateException("not a provider key");
            }
            this.type = ((ParameterizedType) providerKey.type).getActualTypeArguments()[0];
            this.annotations.addAll(providerKey.annotations);
            this.loaders.addAll(providerKey.loaders);
            this.filters.addAll(providerKey.filters);
            this.qualifiers.addAll(providerKey.qualifiers);
            this.hc = $.hc(this.type, this.annotations);
        }

        @Override
        public int hashCode() {
            return hc;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Key) {
                Key that = (Key) obj;
                return that.hc == hc
                        && $.eq(type, that.type)
                        && $.eq2(annotations, that.annotations);
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder sb = S.builder(type());
            if (!annotations().isEmpty()) {
                sb.append("@[").append(S.join(", ", annotations())).append("]");
            }
            return sb.toString();
        }

        Class rawType() {
            if (type instanceof Class) {
                return (Class) type;
            } else if (type instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) type).getRawType();
            } else {
                throw E.unexpected("type not recognized: %s", type);
            }
        }

        Type type() {
            return type;
        }

        boolean isProvider() {
            return Provider.class.isAssignableFrom(rawType());
        }

        Key providerKey() {
            return new Key(this);
        }

        List<Annotation> annotations() {
            return annotations;
        }

        List<Type> typeParams() {
            if (null == typeParams) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType ptype = $.cast(type);
                    Type[] ta = ptype.getActualTypeArguments();
                    typeParams = C.listOf(ta);
                } else {
                    typeParams = C.list();
                }
            }
            return typeParams;
        }

        boolean hasLoader() {
            return !loaders.isEmpty();
        }

        Set<Annotation> loaders() {
            return loaders;
        }

        Set<Annotation> filters() {
            return filters;
        }

        Set<Annotation> loadersAndFilters() {
            Set<Annotation> set = C.newSet(loaders);
            set.addAll(filters);
            return set;
        }

        boolean notConstructable() {
            Class<?> c = rawType();
            return c.isInterface() || c.isArray() || Modifier.isAbstract(c.getModifiers());
        }

        boolean hasQualifier() {
            return !qualifiers.isEmpty();
        }

        Set<Annotation> qualifiers() {
            return qualifiers;
        }

        private void resolve(Annotation[] aa) {
            if (null == aa || aa.length == 0) {
                return;
            }
            // Note only qualifiers and bean loader annotation are considered
            // effective annotation. Scope annotations is not effective here
            // because they are tagged on target type, not the field or method
            // parameter
            for (Annotation anno: aa) {
                Class cls = anno.annotationType();
                if (cls.isAnnotationPresent(Loader.class)) {
                    annotations.add(anno);
                    loaders.add(anno);
                } else if (cls.isAnnotationPresent(Qualifier.class)) {
                    annotations.add(anno);
                    qualifiers.add(anno);
                } else if (cls.isAnnotationPresent(Filter.class)) {
                    annotations.add(anno);
                    filters.add(anno);
                }
            }
            Collections.sort(annotations, ANNO_CMP);
        }

        static Key of(Class<?> clazz) {
            return new Key(clazz, null);
        }

        static Key of(Field field) {
            return new Key(field.getGenericType(), field.getDeclaredAnnotations());
        }

        static Key of(Type type, Annotation[] paramAnnotations) {
            return new Key(type, paramAnnotations);
        }

    }

    private static class FieldInjector {
        private final Field field;
        private final Provider provider;

        FieldInjector(Field field, Provider provider) {
            this.field = field;
            this.provider = provider;
        }

        void applyTo(Object bean) {
            Object obj = provider.get();
            try {
                field.set(bean, obj);
            } catch (Exception e) {
                throw new InjectException(e, "Unable to inject field value on %s", bean.getClass());
            }
        }
    }

    private static class MethodInjector {
        private final Method method;
        private final Provider[] providers;

        MethodInjector(Method method, Provider[] providers) {
            this.method = method;
            this.providers = providers;
        }

        Object applyTo(Object bean) {
            try {
                return method.invoke(bean, params(providers));
            } catch (Exception e) {
                throw new InjectException(e, "Unable to invoke method[%s] on %s", method.getName(), bean.getClass());
            }
        }
    }

    private ConcurrentMap<Key, Provider<?>> registry = new ConcurrentHashMap<Key, Provider<?>>();

    private static final ThreadLocal<Genie> current = new ThreadLocal<Genie>();


    public Genie(Object ... modules) {
        if (modules.length > 0) {
            for (Object module : modules) {
                registerModule(module);
            }
        }
    }

    /**
     * Returns a bean of given type
     * @param type the class of the bean
     * @param <T> generic type of the bean
     * @return the bean
     */
    public <T> T get(Class<T> type) {
        current.set(this);
        Key key = Key.of(type);
        return get(key);
    }

    public static Genie get() {
        return current.get();
    }

    public <T> void registerProvider(Class<T> type, Provider<? extends T> provider) {
        Key key = Key.of(type);
        registerProvider(key, provider);
    }

    private void registerProvider(Key key, Provider<?> provider) {
        Provider previous = registry.putIfAbsent(key, provider);
        if (null != previous) {
            throw new InjectException("Provider has already registered by key: %s", key);
        }
    }

    private void registerModule(Object module) {
        if (module instanceof Module) {
            ((Module) module).applyTo(this);
        }
        Class moduleClass = (module instanceof Class) ? (Class) module : module.getClass();
        for (Method method : moduleClass.getMethods()) {
            if (method.isAnnotationPresent(Provides.class)) {
                registerFactoryMethod(module, method);
            }
        }
    }

    private void registerFactoryMethod(Object module, final Method factory) {
        final Object instance = Modifier.isStatic(factory.getModifiers()) ? null : module;
        Type retType = factory.getGenericReturnType();
        final Key key = Key.of(retType, factory.getDeclaredAnnotations());
        final MethodInjector methodInjector = methodInjector(factory, C.set(key));
        registerProvider(key, new Provider () {
            @Override
            public Object get() {
                return methodInjector.applyTo(instance);
            }
        });
    }

    private <T> T get(Key key) {
        Provider<?> provider = findProvider(key, C.set(key));
        return (T) provider.get();
    }

    private Provider<?> findProvider(final Key key, final Set<Key> chain) {
        // 0. try registry
        Provider<?> provider = registry.get(key);
        if (null != provider) {
            return provider;
        }

        while (true) {
            // 1. try get builder
            provider = getBuilder(key);
            if (null != provider) {
                break;
            }

            // is it a direct Provider?
            if (key.isProvider()) {
                return new Provider<Provider<?>>() {
                    @Override
                    public Provider<?> get() {
                        return findProvider(key.providerKey(), C.<Key>empty());
                    }
                };
            }

            // 2. build provider from constructor, field or method
            if (key.notConstructable()) {
                throw new InjectException("Cannot instantiate %s", key);
            }
            provider = buildProvider(key, chain);
            break;
        }
        registry.putIfAbsent(key, ScopedProvider.scoping(key.type, provider));
        return provider;
    }

    private Builder getBuilder(Key key) {
        if (!key.hasLoader()) {
            return null;
        }
        Class clazz = key.rawType();
        Builder.Factory factory = Builder.Factory.Manager.get(clazz);
        if (null == factory) {
            return null;
        }
        return factory.createBuilder(clazz, key.loadersAndFilters(), key.typeParams());
    }

    private Provider buildProvider(Key key, Set<Key> chain) {
        Class target = key.rawType();
        Constructor constructor = constructor(target);
        return null != constructor ? buildConstructor(constructor, key, chain) : buildFMInjector(target, key, chain);
    }

    private Provider buildConstructor(final Constructor constructor, final Key key, final Set<Key> chain) {
        Type[] ta = constructor.getGenericParameterTypes();
        Annotation[][] aaa = constructor.getParameterAnnotations();
        final Provider[] pp = paramProviders(ta, aaa, chain);
        return new Provider() {
            @Override
            public Object get() {
                try {
                    return constructor.newInstance(params(pp));
                } catch (Exception e) {
                    throw new InjectException(e, "cannot instantiate %s", key);
                }
            }
        };
    }

    private Provider buildFMInjector(final Class target, final Key key, Set<Key> chain) {
        final List<FieldInjector> fieldInjectors = fieldInjectors(target, chain);
        final List<MethodInjector> methodInjectors = methodInjectors(target, chain);
        return new Provider() {
            @Override
            public Object get() {
                try {
                    Constructor constructor = target.getDeclaredConstructor();
                    if (null == constructor) {
                        throw new InjectException("cannot instantiate %s: %s", key, "no default constructor found");
                    }
                    constructor.setAccessible(true);
                    Object bean = constructor.newInstance();
                    for (FieldInjector fj : fieldInjectors) {
                        fj.applyTo(bean);
                    }
                    for (MethodInjector mj : methodInjectors) {
                        mj.applyTo(bean);
                    }
                    return bean;
                } catch (InjectException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InjectException(e, "cannot instantiate %s", key);
                }
            }
        };
    }

    private Constructor constructor(Class target) {
        Constructor[] ca = target.getDeclaredConstructors();
        for (Constructor c: ca) {
            if (c.isAnnotationPresent(Inject.class)) {
                c.setAccessible(true);
                return c;
            }
        }
        return null;
    }

    private List<FieldInjector> fieldInjectors(Class type, Set<Key> chain) {
        Class<?> current = type;
        List<FieldInjector> fieldInjectors = C.newList();
        while (null != current && !current.equals(Object.class)) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    fieldInjectors.add(fieldInjector(field, chain));
                }
            }
            current = current.getSuperclass();
        }
        return fieldInjectors;
    }

    private FieldInjector fieldInjector(Field field, Set<Key> chain) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        Key fieldKey = Key.of(field.getGenericType(), annotations);
        if (chain.contains(fieldKey)) {
            foundCircularDependency(chain, fieldKey);
        }
        return new FieldInjector(field, findProvider(fieldKey, chain(chain, fieldKey)));
    }

    private List<MethodInjector> methodInjectors(Class type, Set<Key> chain) {
        Class<?> current = type;
        List<MethodInjector> methodInjectors = C.newList();
        while (null != current && !current.equals(Object.class)) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Inject.class)) {
                    method.setAccessible(true);
                    methodInjectors.add(methodInjector(method, chain));
                }
            }
            current = current.getSuperclass();
        }
        return methodInjectors;
    }

    private MethodInjector methodInjector(Method method, Set<Key> chain) {
        Type[] paramTypes = method.getGenericParameterTypes();
        int len = paramTypes.length;
        Provider[] paramProviders = new Provider[len];
        Annotation[][] aaa = method.getParameterAnnotations();
        for (int i = 0; i < len; ++i) {
            Type paramType = paramTypes[i];
            Key paramKey = Key.of(paramType, aaa[i]);
            if (chain.contains(paramKey)) {
                foundCircularDependency(chain, paramKey);
            }
            paramProviders[i] = findProvider(paramKey, chain(chain, paramKey));
        }
        return new MethodInjector(method, paramProviders);
    }

    private Provider[] paramProviders(Type[] paramTypes, Annotation[][] aaa, Set<Key> chain) {
        final int len = paramTypes.length;
        Provider[] pa = new Provider[len];
        for (int i = 0; i < len; ++i) {
            Type type = paramTypes[i];
            Annotation[] annotations = aaa[i];
            Key paramKey = Key.of(type, annotations);
            if (chain.contains(paramKey)) {
                foundCircularDependency(chain, paramKey);
            }
            pa[i] = findProvider(paramKey, chain(chain, paramKey));
        }
        return pa;
    }

    private static Object[] params(Provider<?>[] paramProviders) {
        final int len = paramProviders.length;
        Object[] params = new Object[len];
        for (int i = 0; i < len; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private static Set<Key> chain(Set<Key> chain, Key newKey) {
        Set<Key> newChain = C.newSet(chain);
        newChain.add(newKey);
        return newChain;
    }

    private static StringBuilder debugChain(Set<Key> chain, Key last) {
        StringBuilder sb = S.builder();
        for (Key key : chain) {
            sb.append(key).append(" -> ");
        }
        sb.append(last);
        return sb;
    }

    private static void foundCircularDependency(Set<Key> chain, Key lastKey) {
        throw InjectException.circularDependency(debugChain(chain, lastKey));
    }

}
