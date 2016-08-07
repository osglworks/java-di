package org.osgl.inject;

import org.osgl.$;
import org.osgl.exception.UnexpectedException;
import org.osgl.inject.annotation.*;
import org.osgl.inject.provider.*;
import org.osgl.inject.util.AnnotationUtil;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Genie implements Injector {

    static final Logger logger = LogManager.get(Genie.class);

    private static final ThreadLocal<Integer> AFFINITY = new ThreadLocal<Integer>();

    public static class Binder<T> {
        private Class<T> type;
        private String name;
        private Provider<? extends T> provider;
        private List<Annotation> annotations = C.newList();
        private Genie genie;
        private Class<? extends Annotation> scope;

        Binder(Class<T> type) {
            this.type = type;
        }

        public Binder<T> named(String name) {
            this.name = name;
            return this;
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
            BeanSpec spec = BeanSpec.of(type, annotations.toArray(new Annotation[annotations.size()]), name, genie);
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
            if (null == obj) {
                return;
            }
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
    private static final Provider[] NO_PROVIDER = new Provider[0];

    private ConcurrentMap<BeanSpec, Provider<?>> registry = new ConcurrentHashMap<BeanSpec, Provider<?>>();
    private ConcurrentMap<Class, Provider> expressRegistry = new ConcurrentHashMap<Class, Provider>();
    private Set<Class<? extends Annotation>> qualifierRegistry = new HashSet<Class<? extends Annotation>>();
    private Map<Class<? extends Annotation>, Class<? extends Annotation>> scopeAliases = new HashMap<Class<? extends Annotation>, Class<? extends Annotation>>();
    private Map<Class<? extends Annotation>, ScopeCache> scopeProviders = new HashMap<Class<? extends Annotation>, ScopeCache>();
    private ConcurrentMap<Class<? extends Annotation>, PostConstructProcessor<?>> postConstructProcessors = new ConcurrentHashMap<Class<? extends Annotation>, PostConstructProcessor<?>>();
    private final ConcurrentHashMap<$.T2<Method, Class>, Provider[]> paramValueProviders = new ConcurrentHashMap<$.T2<Method, Class>, Provider[]>();
    private ConcurrentMap<Class, BeanSpec> beanSpecLookup = new ConcurrentHashMap<Class, BeanSpec>();

    Genie(Object... modules) {
        this(false, modules);
    }

    Genie(boolean noPlugin, Object... modules) {
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

    public <T> T get(Class<T> type) {
        Provider provider = expressRegistry.get(type);
        if (null == provider) {
            if (type.isArray()) {
                provider = ArrayProvider.of(type, this);
                expressRegistry.putIfAbsent(type, provider);
                return (T) provider.get();
            }
            BeanSpec spec = beanSpecOf(type);
            provider = findProvider(spec, C.<BeanSpec>empty());
            expressRegistry.putIfAbsent(type, provider);
        }
        return (T) provider.get();
    }

    public <T> T get(BeanSpec beanSpec) {
        Provider provider = findProvider(beanSpec, C.<BeanSpec>empty());
        return (T) provider.get();
    }

    @Override
    public Object[] getParams(Method method, $.Func2<BeanSpec, Injector, Provider> ctxParamProviderLookup, Class context) {
        $.T2<Method, Class> key = $.T2(method, context);
        Provider[] pa = paramValueProviders.get(key);
        if (null == pa) {
            pa = buildParamValueProviders(method, ctxParamProviderLookup);
            paramValueProviders.putIfAbsent(key, pa);
        }
        int len = pa.length;
        Object[] params = new Object[len];
        for (int i = 0; i < len; ++i) {
            params[i] = pa[i].get();
        }
        return params;
    }

    public <T> void registerProvider(Class<T> type, Provider<? extends T> provider) {
        AFFINITY.set(0);
        bindProviderToClass(type, provider);
    }

    public void registerQualifiers(Class<? extends Annotation> ... qualifiers) {
        this.qualifierRegistry.addAll(C.listOf(qualifiers));
    }

    public void registerQualifiers(Collection<Class<? extends Annotation>> qualifiers) {
        this.qualifierRegistry.addAll(qualifiers);
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

    public void registerPostConstructProcessor(
            Class<? extends Annotation> annoClass,
            PostConstructProcessor<?> processor
    ) {
        postConstructProcessors.put(annoClass, processor);
    }

    boolean isScope(Class<? extends Annotation> annoClass) {
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

    boolean isQualifier(Class<? extends Annotation> annoClass) {
        return qualifierRegistry.contains(annoClass) || annoClass.isAnnotationPresent(Qualifier.class);
    }

    boolean isPostConstructProcessor(Class<? extends Annotation> annoClass) {
        return postConstructProcessors.containsKey(annoClass) || annoClass.isAnnotationPresent(PostConstructProcess.class);
    }

    PostConstructProcessor postConstructProcessor(Annotation annotation) {
        Class<? extends Annotation> annoClass = annotation.annotationType();
        PostConstructProcessor processor = postConstructProcessors.get(annoClass);
        if (null == processor) {
            if (!annoClass.isAnnotationPresent(PostConstructProcess.class)) {
                throw new UnexpectedException("Cannot find PostConstructProcessor on %s", annoClass);
            }
            PostConstructProcess pcp = annoClass.getAnnotation(PostConstructProcess.class);
            Class<? extends PostConstructProcessor> cls = pcp.value();
            processor = get(cls);
            PostConstructProcessor p2 = postConstructProcessors.putIfAbsent(annoClass, processor);
            if (null != p2) {
                processor = p2;
            }
        }
        return processor;
    }

    private BeanSpec beanSpecOf(Class type) {
        BeanSpec spec = beanSpecLookup.get(type);
        if (null == spec) {
            spec = BeanSpec.of(type, this);
            beanSpecLookup.putIfAbsent(type, spec);
        }
        return spec;
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
        BeanSpec spec = beanSpecOf(type);
        WeightedProvider<?> old = (WeightedProvider<?>) registry.get(spec);
        if (null == old || old.compareTo(current) > 0) {
            registry.put(spec, current);
            expressRegistry.put(type, current);
        }
        if (null != old && old.affinity == 0 && current.affinity == 0) {
            throw new InjectException("Provider has already registered for spec: %s", spec);
        }
    }

    private void registerBuiltInProviders() {
        registerProvider(Collection.class, OsglListProvider.INSTANCE);
        registerProvider(Deque.class, DequeProvider.INSTANCE);
        registerProvider(ArrayList.class, ArrayListProvider.INSTANCE);
        registerProvider(LinkedList.class, LinkedListProvider.INSTANCE);
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
        // try registry
        Provider<?> provider = registry.get(spec);
        if (null != provider) {
            return provider;
        }

        // does it want to inject a Provider?
        if (spec.isProvider()) {
            provider = new Provider<Provider<?>>() {
                @Override
                public Provider<?> get() {
                    return findProvider(spec.toProvidee(), C.<BeanSpec>empty());
                }
            };
            registry.putIfAbsent(spec, provider);
            return provider;
        }

        // does it require a value loading logic
        if (spec.hasValueLoader()) {
            provider = ValueLoaderProvider.create(spec, this);
        } else {
            // does it require an array
            if (spec.isArray()) {
                return ArrayProvider.of(spec, this);
            }
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
        return ScopedProvider.decorate(spec,
                PostConstructProcessorInvoker.decorate(spec,
                        PostConstructorInvoker.decorate(spec,
                                ElementLoaderProvider.decorate(spec, provider, this)
                                , this)
                        , this)
                , this);
    }

    private Provider buildProvider(BeanSpec spec, Set<BeanSpec> chain) {
        Class target = spec.rawType();
        Constructor constructor = constructor(target);
        return null != constructor ? buildConstructor(constructor, spec, chain) : buildFieldMethodInjector(target, spec, chain);
    }

    private Provider buildConstructor(final Constructor constructor, final BeanSpec spec, final Set<BeanSpec> chain) {
        Type[] ta = constructor.getGenericParameterTypes();
        Annotation[][] aaa = constructor.getParameterAnnotations();
        final Provider[] pp = paramProviders(ta, aaa, chain, null);
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
                    } catch (RuntimeException e) {
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

    private Provider[] buildParamValueProviders(Method method, $.Func2<BeanSpec, Injector, Provider> ctxParamProviderLookup) {
        Type[] ta = method.getGenericParameterTypes();
        int len = ta.length;
        if (0 == len) {
            return NO_PROVIDER;
        }
        Annotation[][] aaa = method.getParameterAnnotations();
        Set<BeanSpec> chain = new HashSet<BeanSpec>();
        chain.add(beanSpecOf(method.getDeclaringClass()));
        return paramProviders(ta, aaa, chain, ctxParamProviderLookup);
    }

    private static <T extends Annotation> T filterAnnotation(Annotation[] aa, Class<T> ac) {
        for (Annotation a : aa) {
            if (a.annotationType() == ac) {
                return (T) a;
            }
        }
        return null;
    }

    private Provider[] paramProviders(
            Type[] paramTypes,
            Annotation[][] aaa,
            Set<BeanSpec> chain,
            final $.Func2<BeanSpec, Injector, Provider> ctxParamProviderLookup
    ) {
        final int len = paramTypes.length;
        Provider[] pa = new Provider[len];
        for (int i = 0; i < len; ++i) {
            Type type = paramTypes[i];
            Annotation[] annotations = aaa[i];
            final BeanSpec paramSpec = BeanSpec.of(type, annotations, this);
            if (chain.contains(paramSpec)) {
                foundCircularDependency(chain, paramSpec);
            }
            if (null != ctxParamProviderLookup) {
                if (!paramSpec.hasElementLoader() && null == filterAnnotation(annotations, Provided.class)) {
                    Provider provider;
                    if (paramSpec.hasValueLoader()) {
                        provider = ValueLoaderProvider.create(paramSpec, this);
                    } else {
                        provider = ctxParamProviderLookup.apply(paramSpec, this);
                    }
                    pa[i] = provider;
                } else {
                    pa[i] = findProvider(paramSpec, chain(chain, paramSpec));
                }
            } else {
                pa[i] = findProvider(paramSpec, chain(chain, paramSpec));
            }
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
