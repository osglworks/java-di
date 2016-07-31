package org.osgl.inject;

import org.osgl.$;
import org.osgl.inject.annotation.Provides;
import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;
import org.osgl.inject.provider.*;
import org.osgl.inject.util.AnnotationUtil;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Genie is responsible for providing instance as required
 */
public final class Genie {

    static final Logger logger = LogManager.get(Genie.class);

    private static final ThreadLocal<Integer> AFFINITY = new ThreadLocal<Integer>();

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
            BeanSpec spec = beanSpec(genie);
            genie.registerProvider(spec, genie.decorate(spec, provider));
        }

        BeanSpec beanSpec(Genie genie) {
            BeanSpec spec = BeanSpec.of(type, annotations.toArray(new Annotation[annotations.size()]), genie);
            if (scope != null) {
                spec.scope(scope);
            }
            return spec;
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


        @Override
        public String toString() {
            return $.fmt("Field for %s", field);
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

        @Override
        public String toString() {
            return $.fmt("MethodInjector for %s", method);
        }
    }

    private ConcurrentMap<BeanSpec, Provider<?>> registry = new ConcurrentHashMap<BeanSpec, Provider<?>>();
    private ConcurrentMap<Class, Provider> expressRegistry = new ConcurrentHashMap<Class, Provider>();
    private Map<Class<? extends Annotation>, Class<? extends Annotation>> scopeAliases = new HashMap<Class<? extends Annotation>, Class<? extends Annotation>>();
    private Map<Class<? extends Annotation>, ScopeCache> scopeProviders = new HashMap<Class<? extends Annotation>, ScopeCache>();

    Genie(Object... modules) {
        this(false, modules);
    }

    Genie(boolean noPlugin, Object ... modules) {
        registerBuiltInProviders();
        if (!noPlugin) {
            registerBuiltInPlugins();
        }
        if (modules.length > 0) {
            for (Object module : modules) {
                registerModule(module);
            }
        }
    }

    /**
     * Returns a bean of given type
     *
     * @param type the class of the bean
     * @param <T>  generic type of the bean
     * @return the bean
     */
    public <T> T get(Class<T> type) {
        Provider provider = expressRegistry.get(type);
        if (null == provider) {
            BeanSpec spec = BeanSpec.of(type, this);
            provider = findProvider(spec, C.<BeanSpec>empty());
            expressRegistry.putIfAbsent(type, provider);
        }
        return (T)provider.get();
    }

    public <T> T get(BeanSpec spec) {
        Provider<?> provider = findProvider(spec, C.<BeanSpec>empty());
        return (T) provider.get();
    }

    /**
     * Returns a bean of given type and annotations. This is helpful
     * when it needs to inject a value for a method parameter
     * @param type the type of the bean
     * @param annotations the annotations tagged to the (parameter)
     * @param <T> the generic type
     * @return the bean instance
     */
    public <T> T get(Type type, Annotation[] annotations) {
        return get(BeanSpec.of(type, annotations, this));
    }

    /**
     * Returns parameter as bean for a given method
     * @param method the method
     * @return the parameters that an be used to invoke the method
     */
    public Object[] getParams(Method method) {
        Type[] ta = method.getGenericParameterTypes();
        int len = ta.length;
        Object[] oa = new Object[ta.length];
        if (0 == len) {
            return oa;
        }
        Annotation[][] aaa = method.getParameterAnnotations();
        Set<BeanSpec> chain = new HashSet<BeanSpec>();
        chain.add(BeanSpec.of(method.getDeclaringClass(), this));
        final Provider[] pa = paramProviders(ta, aaa, chain);
        for (int i = 0; i < len; ++i) {
            oa[i] = pa[i].get();
        }
        return oa;
    }

    public <T> void registerProvider(Class<T> type, Provider<? extends T> provider) {
        AFFINITY.set(0);
        bindProviderToClass(type, provider);
    }

    public void registerScopeAlias(Class<? extends Annotation> scopeAnnotation, Class<? extends Annotation> scopeAlias) {
        scopeAliases.put(scopeAlias, scopeAnnotation);
    }

    public void registerScopeProvider(Class<? extends Annotation> scopeAnnotation, ScopeCache scopeCache) {
        scopeProviders.put(scopeAnnotation, scopeCache);
    }

    public void registerScopeProvider(Class<? extends Annotation> scopeAnnotation, Class<? extends ScopeCache> scopeCacheClass) {
        scopeProviders.put(scopeAnnotation, get(scopeCacheClass));
    }

    boolean isScopeAnnotation(Class<? extends Annotation> annoClass) {
        if (Singleton.class == annoClass || SessionScoped.class == annoClass || RequestScoped.class == annoClass) {
            return true;
        }
        return scopeAliases.containsKey(annoClass) || scopeProviders.containsKey(annoClass);
    }

    Class<? extends Annotation> scopeByAlias(Class<? extends Annotation> alias) {
        return scopeAliases.get(alias);
    }

    ScopeCache scopeCache(Class<? extends Annotation> scope) {
        return scopeProviders.get(scope);
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
        for (Class role : roles) {
            bindProviderToClass(role, provider);
        }
    }

    private void addIntoRegistry(Class<?> type, Provider<?> val) {
        WeightedProvider current = WeightedProvider.decorate(val);
        BeanSpec spec = BeanSpec.of(type, this);
        WeightedProvider<?> old = (WeightedProvider<?>) registry.get(spec);
        if (null == old || old.compareTo(current) > 0) {
            registry.put(spec, current);
        }
        if (null != old && old.affinity == 0 && current.affinity == 0) {
            throw new InjectException("Provider has already registered for spec: %s", spec);
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

    private void registerBuiltInPlugins() {
        tryRegisterPlugin("org.osgl.inject.CDIAdaptor");
        tryRegisterPlugin("org.osgl.inject.GuiceAdaptor");
    }

    private void tryRegisterPlugin(String pluginClass) {
        try {
            GeniePlugin plugin = $.newInstance(pluginClass);
            plugin.register(this);
        } catch (Exception e) {
            logger.warn(e, "error registering plug: %s", pluginClass);
        } catch (NoClassDefFoundError e) {
            // plugin dependency not provided, ignore it
        }
    }

    private void registerProvider(BeanSpec spec, Provider<?> provider) {
        Provider previous = registry.putIfAbsent(spec, provider);
        if (null != previous) {
            if (previous instanceof WeightedProvider) {
                previous = ((WeightedProvider) previous).realProvider;
            }
            String newName = provider.getClass().getName();
            if (newName.contains("org.osgl.inject.Genie$")) {
                newName = provider.toString();
            }
            logger.warn("Provider %s \n\tfor [%s] \n\tis replaced with: %s", previous.getClass().getName(), spec, newName);
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
        final BeanSpec spec = BeanSpec.of(retType, factory.getAnnotations(), this);
        final MethodInjector methodInjector = methodInjector(factory, C.<BeanSpec>empty());
        registerProvider(spec, decorate(spec, new Provider() {
            @Override
            public Object get() {
                return methodInjector.applyTo(instance);
            }

            @Override
            public String toString() {
                return S.fmt("%s::%s", instance.getClass().getName(), methodInjector.method.getName());
            }
        }));
    }

    private Provider<?> findProvider(final BeanSpec spec, final Set<BeanSpec> chain) {
        // 0. try registry
        Provider<?> provider = registry.get(spec);
        if (null != provider) {
            return provider;
        }

        // does it want to inject a Provider?
        if (spec.isProvider()) {
            provider = new Provider<Provider<?>>() {
                @Override
                public Provider<?> get() {
                    return findProvider(spec.providerSpec(), C.<BeanSpec>empty());
                }
            };
            registry.putIfAbsent(spec, provider);
            return provider;
        }

        // does it require a value loading logic
        if (spec.isValueLoad()) {
            provider = ValueLoaderProvider.create(spec, this);
        } else {
            // build provider from constructor, field or method
            if (spec.notConstructable()) {
                // does spec's bare class have provider?
                provider = registry.get(spec.rawTypeSpec());
                if (null == provider) {
                    throw new InjectException("Cannot instantiate %s", spec);
                }
            } else {
                provider = buildProvider(spec, chain);
            }
        }
        Provider<?> decorated = decorate(spec, provider);
        registry.putIfAbsent(spec, decorated);
        return decorated;
    }

    private Provider<?> decorate(BeanSpec spec, Provider provider) {
        return ScopedProvider.decorate(spec, PostConstructorInvoker.decorate(spec, ElementLoaderProvider.decorate(spec, provider, this), this), this);
    }

    private Provider buildProvider(BeanSpec spec, Set<BeanSpec> chain) {
        Class target = spec.rawType();
        Constructor constructor = constructor(target);
        return null != constructor ? buildConstructor(constructor, spec, chain) : buildFieldMethodInjector(target, spec, chain);
    }

    private Provider buildConstructor(final Constructor constructor, final BeanSpec spec, final Set<BeanSpec> chain) {
        Type[] ta = constructor.getGenericParameterTypes();
        Annotation[][] aaa = constructor.getParameterAnnotations();
        final Provider[] pp = paramProviders(ta, aaa, chain);
        return new Provider() {
            @Override
            public Object get() {
                try {
                    return constructor.newInstance(params(pp));
                } catch (Exception e) {
                    throw new InjectException(e, "cannot instantiate %s", spec);
                }
            }
        };
    }

    private Provider buildFieldMethodInjector(final Class target, final BeanSpec spec, Set<BeanSpec> chain) {
        final List<FieldInjector> fieldInjectors = fieldInjectors(target, chain);
        final List<MethodInjector> methodInjectors = methodInjectors(target, chain);
        try {
            final Constructor constructor = target.getDeclaredConstructor();
            if (null == constructor) {
                throw new InjectException("cannot instantiate %s: %s", spec, "no default constructor found");
            }
            constructor.setAccessible(true);
            return new Provider() {
                @Override
                public Object get() {
                    try {
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
                        throw new InjectException(e, "cannot instantiate %s", spec);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            throw new InjectException(e, "cannot instantiate %s", spec);
        }
    }

    private Constructor constructor(Class target) {
        Constructor[] ca = target.getDeclaredConstructors();
        for (Constructor c : ca) {
            if (c.isAnnotationPresent(Inject.class)) {
                c.setAccessible(true);
                return c;
            }
        }
        return null;
    }

    private List<FieldInjector> fieldInjectors(Class type, Set<BeanSpec> chain) {
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

    private FieldInjector fieldInjector(Field field, Set<BeanSpec> chain) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        BeanSpec fieldSpec = BeanSpec.of(field.getGenericType(), annotations, this);
        if (chain.contains(fieldSpec)) {
            foundCircularDependency(chain, fieldSpec);
        }
        return new FieldInjector(field, findProvider(fieldSpec, chain(chain, fieldSpec)));
    }

    private List<MethodInjector> methodInjectors(Class type, Set<BeanSpec> chain) {
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

    private MethodInjector methodInjector(Method method, Set<BeanSpec> chain) {
        Type[] paramTypes = method.getGenericParameterTypes();
        int len = paramTypes.length;
        Provider[] paramProviders = new Provider[len];
        Annotation[][] aaa = method.getParameterAnnotations();
        for (int i = 0; i < len; ++i) {
            Type paramType = paramTypes[i];
            BeanSpec paramSpec = BeanSpec.of(paramType, aaa[i], this);
            if (chain.contains(paramSpec)) {
                foundCircularDependency(chain, paramSpec);
            }
            paramProviders[i] = findProvider(paramSpec, chain(chain, paramSpec));
        }
        return new MethodInjector(method, paramProviders);
    }

    private Provider[] paramProviders(Type[] paramTypes, Annotation[][] aaa, Set<BeanSpec> chain) {
        final int len = paramTypes.length;
        Provider[] pa = new Provider[len];
        for (int i = 0; i < len; ++i) {
            Type type = paramTypes[i];
            Annotation[] annotations = aaa[i];
            BeanSpec paramSpec = BeanSpec.of(type, annotations, this);
            if (chain.contains(paramSpec)) {
                foundCircularDependency(chain, paramSpec);
            }
            pa[i] = findProvider(paramSpec, chain(chain, paramSpec));
        }
        return pa;
    }

    /**
     * Create a Genie instance with modules specified
     *
     * @param modules modules that provides binding or {@literal@}Provides methods
     * @return an new Genie instance with modules
     */
    public static Genie create(Object... modules) {
        return new Genie(modules);
    }

    /**
     * Create a Genie instance with modules specified
     *
     * @param modules modules that provides binding or {@literal@}Provides methods
     * @return an new Genie instance with modules
     */
    public static Genie createWithoutPlugins(Object... modules) {
        return new Genie(true, modules);
    }

    private static Object[] params(Provider<?>[] paramProviders) {
        final int len = paramProviders.length;
        Object[] params = new Object[len];
        for (int i = 0; i < len; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private static Set<BeanSpec> chain(Set<BeanSpec> chain, BeanSpec nextSpec) {
        Set<BeanSpec> newChain = new HashSet<BeanSpec>(chain);
        newChain.add(nextSpec);
        return newChain;
    }

    private static StringBuilder debugChain(Set<BeanSpec> chain, BeanSpec last) {
        StringBuilder sb = S.builder();
        for (BeanSpec spec : chain) {
            sb.append(spec).append(" -> ");
        }
        sb.append(last);
        return sb;
    }

    private static void foundCircularDependency(Set<BeanSpec> chain, BeanSpec last) {
        throw InjectException.circularDependency(debugChain(chain, last));
    }

}
