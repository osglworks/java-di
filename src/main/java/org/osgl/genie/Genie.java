package org.osgl.genie;

import org.osgl.$;
import org.osgl.genie.annotation.Provides;
import org.osgl.genie.provider.*;
import org.osgl.genie.spi.ScopeResolver;
import org.osgl.genie.util.AnnotationUtil;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Scope;
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
            BeanSpec spec = beanSpec();
            genie.registerProvider(spec, genie.decorate(spec, provider));
        }

        BeanSpec beanSpec() {
            BeanSpec spec = new BeanSpec(type, annotations.toArray(new Annotation[annotations.size()]));
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

    private ConcurrentMap<BeanSpec, Provider<?>> registry = new ConcurrentHashMap<BeanSpec, Provider<?>>();

    private static final ThreadLocal<Integer> AFFINITY = new ThreadLocal<Integer>();

    Genie(Object... modules) {
        registerBuiltInProviders();
        if (modules.length > 0) {
            for (Object module : modules) {
                registerModule(module);
            }
        }
        BeanSpec.scopeResolver(get(ScopeResolver.class));
    }

    /**
     * Returns a bean of given type
     *
     * @param type the class of the bean
     * @param <T>  generic type of the bean
     * @return the bean
     */
    public <T> T get(Class<T> type) {
        BeanSpec spec = BeanSpec.of(type);
        return get(spec);
    }

    public <T> T get(BeanSpec spec) {
        Provider<?> provider = findProvider(spec, C.set(spec));
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
        return get(BeanSpec.of(type, annotations));
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
        final Provider[] pa = paramProviders(ta, aaa, C.set(BeanSpec.of(method.getDeclaringClass())));
        for (int i = 0; i < len; ++i) {
            oa[i] = pa[i].get();
        }
        return oa;
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
        for (Class role : roles) {
            bindProviderToClass(role, provider);
        }
    }

    private void addIntoRegistry(Class<?> type, Provider<?> val) {
        WeightedProvider current = WeightedProvider.decorate(val);
        BeanSpec spec = BeanSpec.of(type);
        WeightedProvider<?> old = (WeightedProvider<?>) registry.get(spec);
        if (null == old || old.compareTo(current) > 0) {
            registry.put(spec, current);
        }
        if (null != old && old.affinity == 0 && current.affinity == 0) {
            throw new InjectException("Provider has already registered for spec: %s", spec);
        }
    }

    private void registerBuiltInProviders() {
        registerProvider(ScopeResolver.class, ScopeResolver.BuiltInScopeResolver.INSTANCE);
        registerProvider(Collection.class, OsglListProvider.INSTANCE);
        registerProvider(Deque.class, DequeProvider.INSTANCE);
        registerProvider(C.List.class, OsglListProvider.INSTANCE);
        registerProvider(C.Set.class, OsglSetProvider.INSTANCE);
        registerProvider(C.Map.class, OsglMapProvider.INSTANCE);
        registerProvider(ConcurrentMap.class, ConcurrentMapProvider.INSTANCE);
        registerProvider(SortedMap.class, SortedMapProvider.INSTANCE);
        registerProvider(SortedSet.class, SortedSetProvider.INSTANCE);
    }

    private void registerProvider(BeanSpec spec, Provider<?> provider) {
        Provider previous = registry.putIfAbsent(spec, provider);
        if (null != previous) {
            if (previous instanceof WeightedProvider) {
                previous = ((WeightedProvider) previous).realProvider;
            }
            String newName = provider.getClass().getName();
            if (newName.contains("org.osgl.genie.Genie$")) {
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
        final BeanSpec spec = BeanSpec.of(retType, factory.getAnnotations());
        final MethodInjector methodInjector = methodInjector(factory, C.set(spec));
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
            // no element loader decorating here for obvious reason
            // no scoping decorating here because it is already decorated
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

            provider = decorate(spec, provider);
        }

        registry.putIfAbsent(spec, provider);
        return provider;
    }

    private Provider<?> decorate(BeanSpec spec, Provider provider) {
        return ScopedProvider.decorate(spec, ElementLoaderProvider.decorate(spec, provider, this), this);
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
        return new Provider() {
            @Override
            public Object get() {
                try {
                    Constructor constructor = target.getDeclaredConstructor();
                    if (null == constructor) {
                        throw new InjectException("cannot instantiate %s: %s", spec, "no default constructor found");
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
                    throw new InjectException(e, "cannot instantiate %s", spec);
                }
            }
        };
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
        BeanSpec fieldSpec = BeanSpec.of(field.getGenericType(), annotations);
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
            BeanSpec paramSpec = BeanSpec.of(paramType, aaa[i]);
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
            BeanSpec paramSpec = BeanSpec.of(type, annotations);
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

    private static Object[] params(Provider<?>[] paramProviders) {
        final int len = paramProviders.length;
        Object[] params = new Object[len];
        for (int i = 0; i < len; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private static Set<BeanSpec> chain(Set<BeanSpec> chain, BeanSpec nextSpec) {
        Set<BeanSpec> newChain = C.newSet(chain);
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
