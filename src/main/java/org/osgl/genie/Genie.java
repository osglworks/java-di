package org.osgl.genie;

import org.osgl.$;
import org.osgl.Osgl.Var;
import org.osgl.genie.annotation.*;
import org.osgl.genie.provider.*;
import org.osgl.genie.util.AnnotationUtil;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.inject.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Genie is responsible for providing instance as required
 */
public final class Genie implements DependencyInjector {

    static final Logger logger = LogManager.get(Genie.class);

    public static class Binder<T> {
        private Class<T> type;
        private Provider<? extends T> provider;
        private List<Annotation> annotations = C.newList();
        private Genie genie;
        private Class<? extends Annotation> scope;
        Binder(Class<T> type) {
            this.type = type;
        }

        public Binder<T> to(final Class<? extends T> impl) {
            this.provider = new Provider<T>() {
                @Override
                public T get() {
                    return genie.get(impl);
                }
            };
            return this;
        }

        public Binder<T> to(Provider<? extends T> provider) {
            this.provider = provider;
            return this;
        }

        public Binder<T> in(Class<? extends Annotation> scope) {
            if (!scope.isAnnotationPresent(Scope.class)) {
                throw new InjectException("Annotation class passed to \"in\" method must have @Scope annotation presented");
            }
            this.scope = scope;
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
            this.genie = genie;
            Key key = key();
            genie.registerProvider(key, genie.decorate(key, provider));
        }

        Key key() {
            Key key = new Key(type, annotations.toArray(new Annotation[annotations.size()]));
            if (scope != null) {
                key.scope.set(scope);
            }
            return key;
        }
    }

    static class Key {

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
        private final Var<MapKey> mapKey = $.var();
        private final Var<Class<? extends Annotation>> scope = $.var();

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
            this.resolveType();
            this.resolveAnnotations(annotations);
            this.hc = $.hc(type, this.annotations);
        }

        private Key(Key providerKey) {
            if (!providerKey.isProvider()) {
                throw new IllegalStateException("not a provider key");
            }
            this.type = ((ParameterizedType) providerKey.type).getActualTypeArguments()[0];
            this.annotations.addAll(providerKey.annotations);
            this.loaders.addAll(providerKey.loaders);
            this.filters.addAll(providerKey.filters);
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

        Key rawTypeKey() {
            return Key.of(rawType());
        }

        Type type() {
            return type;
        }

        boolean isMap() {
            return Map.class.isAssignableFrom(rawType());
        }

        MapKey mapKey() {
            return mapKey.get();
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

        Class<? extends Annotation> scope() {
            return scope.get();
        }

        boolean notConstructable() {
            Class<?> c = rawType();
            return c.isInterface() || c.isArray() || Modifier.isAbstract(c.getModifiers());
        }

        private void resolveType() {
            for (Annotation annotation : rawType().getAnnotations()) {
                resolveScope(annotation);
            }
        }

        private void resolveAnnotations(Annotation[] aa) {
            if (null == aa || aa.length == 0) {
                return;
            }
            Class<?> rawType = rawType();
            boolean isMap = Map.class.isAssignableFrom(rawType);
            boolean isContainer = Collection.class.isAssignableFrom(rawType) || isMap;
            MapKey mapKey = null;
            // Note only qualifiers and bean loaders annotation are considered
            // effective annotation. Scope annotations is not effective here
            // because they are tagged on target type, not the field or method
            // parameter
            for (Annotation anno: aa) {
                Class cls = anno.annotationType();
                if (Inject.class == cls || Provides.class == cls) {
                    continue;
                }
                if (cls == MapKey.class) {
                    if (null != mapKey) {
                        throw new InjectException("MapKey annotation already presented");
                    }
                    if (!isMap) {
                        logger.warn("MapKey annotation ignored on target that is not of Map type");
                    } else {
                        mapKey = $.cast(anno);
                    }
                } if (cls.isAnnotationPresent(Loader.class)) {
                    if (isContainer) {
                        annotations.add(anno);
                        loaders.add(anno);
                    } else {
                        logger.warn("Loader annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                    }
                } else if (cls.isAnnotationPresent(Qualifier.class)) {
                    annotations.add(anno);
                } else if (cls.isAnnotationPresent(Filter.class)) {
                    if (isContainer) {
                        annotations.add(anno);
                        filters.add(anno);
                    } else {
                        logger.warn("Filter annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                    }
                } else if (cls.isAnnotationPresent(Scope.class)) {
                    resolveScope(anno);
                }
            }
            if (isMap && hasLoader() && null == mapKey) {
                throw new InjectException("No MapKey annotation found on Map type target with ElementLoader annotation presented");
            }
            if (null != mapKey) {
                if (hasLoader()) {
                    this.mapKey.set(mapKey);
                } else {
                    logger.warn("MapKey annotation ignored on target without ElementLoader annotation presented");
                }
            }

            Collections.sort(annotations, ANNO_CMP);
        }

        private void resolveScope(Annotation annotation) {
            Class<? extends Annotation> annoClass = annotation.annotationType();
            if (annoClass.isAnnotationPresent(Scope.class)) {
                if (null != scope.get()) {
                    throw new InjectException("Multiple Scope annotation found: %s", this);
                }
                scope.set(annoClass);
            }
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

    private static class WeightedProvider<T> implements Provider<T>, Comparable<WeightedProvider<T>> {
        private Provider<T> realProvider;
        private int affinity;

        WeightedProvider(Provider<T> provider) {
            realProvider = provider;
            affinity = AFFINITY.get();
        }

        @Override
        public T get() {
            return realProvider.get();
        }

        @Override
        public int compareTo(WeightedProvider<T> o) {
            return this.affinity - o.affinity;
        }


        static <T> WeightedProvider<T> decorate(Provider<T> provider) {
            return provider instanceof WeightedProvider ? (WeightedProvider<T>) provider : new WeightedProvider<T>(provider);
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

    private static final ThreadLocal<Integer> AFFINITY = new ThreadLocal<Integer>();

    Genie(Object ... modules) {
        registerBuiltInProviders();
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
        Key key = Key.of(type);
        return get(key);
    }

    public <T> void registerProvider(Class<T> type, Provider<? extends T> provider) {
        AFFINITY.set(0);
        bindProviderToClass(type, provider);
    }

    private void bindProviderToClass(Class<?> target, Provider<?> provider) {
        addIntoRegistry(target, provider);
        AFFINITY.set(AFFINITY.get() + 1);
        Class dad = target.getSuperclass();
        if (null != dad && Object.class != dad) {
            bindProviderToClass(dad, provider);
        }
        Class[] roles = target.getInterfaces();
        if (null == roles) {
            return;
        }
        for (Class role: roles) {
            bindProviderToClass(role, provider);
        }
    }

    private void addIntoRegistry(Class<?> type, Provider<?> val) {
        WeightedProvider current = WeightedProvider.decorate(val);
        Key key = Key.of(type);
        WeightedProvider<?> old = (WeightedProvider<?>) registry.get(key);
        if (null == old || old.compareTo(current) > 0) {
            registry.put(key, current);
        }
        if (null != old && old.affinity == 0 && current.affinity == 0) {
            throw new InjectException("Provider has already registered by key: %s", key);
        }
    }

    private void registerBuiltInProviders() {
        registerProvider(Collection.class, OsglListProvider.INSTANCE);
        registerProvider(Deque.class, DequeProvider.INSTANCE);
        registerProvider(C.List.class, OsglListProvider.INSTANCE);
        registerProvider(C.Set.class, OsglSetProvider.INSTANCE);
        registerProvider(C.Map.class, OsglMapProvider.INSTANCE);
        registerProvider(ConcurrentMap.class, ConcurrentMapProvider.INSTANCE);
        registerProvider(SortedMap.class, SortedMapProvider.INSTANCE);
        registerProvider(SortedSet.class, SortedSetProvider.INSTANCE);
    }

    private void registerProvider(Key key, Provider<?> provider) {
        Provider previous = registry.putIfAbsent(key, provider);
        if (null != previous) {
            throw new InjectException("Provider has already registered by key: %s", key);
        }
    }

    private void registerModule(Object module) {
        boolean isClass = module instanceof Class;
        Class moduleClass = isClass ? (Class) module : module.getClass();
        if (Module.class.isAssignableFrom(moduleClass)) {
            if (isClass) {
                module = $.newInstance(moduleClass);
                isClass = false;
            }
            ((Module) module).applyTo(this);
        }

        for (Method method : moduleClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Provides.class)) {
                method.setAccessible(true);
                boolean isStatic = Modifier.isStatic(method.getModifiers());
                if (isClass && !isStatic) {
                    module = $.newInstance(moduleClass);
                    isClass = false;
                }
                registerFactoryMethod(isStatic ? null : module, method);
            }
        }
    }

    private void registerFactoryMethod(final Object instance, final Method factory) {
        Type retType = factory.getGenericReturnType();
        final Key key = Key.of(retType, factory.getAnnotations());
        final MethodInjector methodInjector = methodInjector(factory, C.set(key));
        registerProvider(key, decorate(key, new Provider () {
            @Override
            public Object get() {
                return methodInjector.applyTo(instance);
            }
        }));
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

        // does it want to inject a Provider?
        if (key.isProvider()) {
            provider = new Provider<Provider<?>>() {
                @Override
                public Provider<?> get() {
                    return findProvider(key.providerKey(), C.<Key>empty());
                }
            };
            registry.putIfAbsent(key, provider);
            return provider;
        }


        // build provider from constructor, field or method
        if (key.notConstructable()) {
            // does key's bare class have provider?
            provider = registry.get(key.rawTypeKey());
            if (null == provider) {
                throw new InjectException("Cannot instantiate %s", key);
            }
        } else {
            provider = buildProvider(key, chain);
        }

        provider = decorate(key, provider);
        registry.putIfAbsent(key, provider);
        return provider;
    }

    private Provider<?> decorate(Key key, Provider provider) {
        return ScopedProvider.decorate(key, ElementLoaderProvider.decorate(key, provider, this), this);
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

    /**
     * Create a Genie instance with modules specified
     * @param modules modules that provides binding or {@literal@}Provides methods
     * @return an new Genie instance with modules
     */
    public static Genie create(Object ... modules) {
        return new Genie(modules);
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
