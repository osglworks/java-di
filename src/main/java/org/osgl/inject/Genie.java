package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgl.$;
import org.osgl.exception.UnexpectedException;
import org.osgl.inject.annotation.*;
import org.osgl.inject.provider.*;
import org.osgl.inject.util.AnnotationUtil;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;
import osgl.version.Version;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.*;

public final class Genie implements Injector {

    /**
     * Describe the version of genie library.
     */
    public static final Version VERSION = Version.of(Genie.class);

    /**
     * The logger used by genie library.
     */
    static final Logger logger = LogManager.get(Genie.class);

    private static final ThreadLocal<BeanSpec> TGT_SPEC = new ThreadLocal<BeanSpec>();
    private static final ThreadLocal<Integer> AFFINITY = new ThreadLocal<Integer>();
    private static final Provider<BeanSpec> BEAN_SPEC_PROVIDER = new Provider<BeanSpec>() {
        @Override
        public BeanSpec get() {
            return TGT_SPEC.get();
        }
    };

    /**
     * A `Binder` is used in {@link Module#configure() module configure method}
     * to define a custom binding.
     *
     * @param <T>
     *           A generic type of binding object
     */
    public static class Binder<T> {
        private Class<T> type;
        private String name;
        private Provider<? extends T> provider;
        private List<Annotation> annotations = C.newList();
        private Class<? extends Annotation> scope;
        private boolean forceFireEvent;
        private boolean fireEvent;
        private Constructor<? extends T> constructor;
        private Class<? extends T> impl;

        /**
         * Create a `Binder` for specified class
         * @param type the type to be bound
         */
        public Binder(Class<T> type) {
            this.type = type;
            this.fireEvent = true;
        }

        /**
         * Bind this `Binder` to a specific implementation.
         *
         * If there is another binding already specified then it
         * will throw out an {@link IllegalStateException}
         *
         * @param impl
         *      the implementation class
         * @return
         *      this binder instance
         * @throws IllegalStateException
         *      if another binding exists
         */
        public Binder<T> to(final Class<? extends T> impl) {
            ensureNoBinding();
            this.impl = $.notNull(impl);
            return this;
        }

        /**
         * Bind this `Binder` to a specific instance
         *
         * If there is another binding already specified then it
         * will throw out an {@link IllegalStateException}
         *
         * @param instance
         *      the instance to which the Binder is bound
         * @return
         *      this binder instance
         * @throws IllegalStateException
         *      if another binding exists
         */
        public Binder<T> to(final T instance) {
            ensureNoBinding();
            this.provider = new Provider<T>() {
                @Override
                public T get() {
                    return instance;
                }
            };
            return this;
        }

        /**
         * Bind this `Binder` to a provider.
         *
         * If there is another binding already specified then it
         * will throw out an {@link IllegalStateException}
         *
         * @param provider
         *      the provider that provides the instance to which this binder is bound
         * @return
         *      this binder instance
         * @throws IllegalStateException
         *      if another binding exists
         */
        public Binder<T> to(Provider<? extends T> provider) {
            ensureNoBinding();
            this.provider = provider;
            return this;
        }

        /**
         * Bind this binder to a constructor
         *
         * If there is another binding already specified then it
         * will throw out an {@link IllegalStateException}
         *
         * @param constructor
         *      the constructor that generate the instance to which this binder is bound
         * @return
         *      this binder instance
         * @throws IllegalStateException
         *      if another binding exists
         */
        public Binder<T> to(final Constructor<? extends T> constructor) {
            ensureNoBinding();
            this.constructor = constructor;
            return this;
        }

        /**
         * Bind this instance to a constructor specified by class and constructor arguments.
         *
         * This is a convenient method for {@link #to(Constructor) constructor binding} as
         * the developer does not need to provides a {@link Constructor} instance
         *
         * If no constructor found with the class and argument types then an
         * {@link InjectException} will be thrown out
         *
         * If there is another binding already specified then it
         * will throw out an {@link IllegalStateException}
         *
         * @param implement
         *      the class of the target instance
         * @param args
         *      the constructor argument types
         * @return
         *      this binder instance
         * @throws
         *      InjectException if no constructor found by the spec
         * @throws IllegalStateException
         *      if another binding exists
         */
        public Binder<T> toConstructor(Class<? extends T> implement, Class<?> ... args) {
            ensureNoBinding();
            try {
                return to(implement.getConstructor(args));
            } catch (NoSuchMethodException e) {
                throw new InjectException(e,
                        "cannot find constructor for %s with arguments: %s", implement.getName(), $.toString2(args));
            }
        }

        private void ensureNoBinding() {
            E.illegalStateIf(bound(), "binding has already been specified");
        }

        /**
         * Constraint the binding with a name.
         *
         * A name is usually specified via {@link Named} annotation when declaring
         * an injection. When genie looking for a provider for a named injection,
         * it will check if the supplied binding matches the name specified.
         *
         * If there is a name already registered it will throw out an
         * {@link IllegalArgumentException}
         *
         * @param name
         *      the name of the binding
         * @return
         *      this `Binder` instance
         * @throws IllegalArgumentException
         *      if there is name already registered
         */
        public Binder<T> named(String name) {
            E.illegalStateIf(null != this.name, "name has already been specified");
            this.name = name;
            this.fireEvent = false;
            return this;
        }

        /**
         * Constraint the binder with a scope annotation class.
         *
         * Once the constraint is added to this binder the binding will search
         * for candidates within the scope constraint only
         *
         * The scope annotation class must be tagged with {@link Scope} annotation.
         * Otherwise a {@link InjectException} will be thrown out.
         *
         * @param scope
         *      the scope annotation class
         * @return
         *      this binder instance
         * @throws InjectException
         *      if the scope class is not annotated with {@link Scope}
         * @throws IllegalStateException
         *      if there is already a scope annotation constraint put on this binder
         * @see Scope
         */
        public Binder<T> in(Class<? extends Annotation> scope) {
            if (!scope.isAnnotationPresent(Scope.class)) {
                throw new InjectException(
                        "the scope annotation type must have @Scope annotation presented: " + scope.getName());
            }
            E.illegalStateIf(null != this.scope, "Scope has already been specified");
            this.scope = scope;
            this.fireEvent = false;
            return this;
        }

        /**
         * Add annotation constraints to this binder.
         *
         * Usually the annotation specified in the parameter should be a valid {@link Qualifier qualifiers}
         *
         * This method is deprecated. Please use {@link #qualifiedWith(Class[])} instead
         *
         * @param annotations
         *      an array of annotation classes
         * @return
         *      this binder instance
         * @see Qualifier
         */
        @Deprecated
        public Binder<T> withAnnotation(Class<? extends Annotation> ... annotations) {
            for (Class<? extends Annotation> annotation : annotations) {
                this.annotations.add(AnnotationUtil.createAnnotation(annotation));
            }
            this.fireEvent = false;
            return this;
        }

        /**
         * Add annotation constraints to this binder.
         *
         * Usually the type of the annotation specified in the parameter should be
         * a valid {@link Qualifier qualifiers}
         *
         * This method is deprecated. Please use {@link #qualifiedWith(Annotation...)} instead
         *
         * @param annotations
         *      an array of annotation classes
         * @return
         *      this binder instance
         * @see Qualifier
         */
        @Deprecated
        public Binder<T> withAnnotation(Annotation ... annotations) {
            this.annotations.addAll(C.listOf(annotations));
            this.fireEvent = false;
            return this;
        }

        /**
         * Add qualifier annotation constraints to this binder
         *
         * Each qualifier annotation type must be tagged with {@link Qualifier} annotation.
         * Otherwise an {@link InjectException} will be thrown out
         *
         * @param qualifiers
         *      an array of qualifier annotation types
         * @return
         *      this binder instance
         * @throws InjectException
         *      if the any qualifier class is not tagged with {@link Qualifier}
         * @see Qualifier
         */
        public Binder<T> qualifiedWith(Class<? extends Annotation> ... qualifiers) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (!qualifier.isAnnotationPresent(Qualifier.class)) {
                    throw new InjectException(
                            "Qualifier annotation type must have \"@Qualifier\" annotation presented: " +
                                    qualifier.getName());
                }
                this.annotations.add(AnnotationUtil.createAnnotation(qualifier));
            }
            this.fireEvent = false;
            return this;
        }

        /**
         * Add qualifier annotation constraints to this binder
         *
         * The type of each qualifier annotation must be tagged with {@link Qualifier}
         * annotation. Otherwise an {@link InjectException} will be thrown out
         *
         * @param qualifiers
         *      an array of qualifier annotations
         * @return
         *      this binder instance
         * @throws InjectException
         *      if the any qualifier's class is not tagged with {@link Qualifier}
         * @see Qualifier
         */
        public Binder<T> qualifiedWith(Annotation... qualifiers) {
            for (Annotation qualifier : qualifiers) {
                Class<? extends Annotation> qulifierType = qualifier.annotationType();
                if (!qulifierType.isAnnotationPresent(Qualifier.class)) {
                    throw new InjectException(
                            "Qualifier annotation type must have \"@Qualifier\" annotation presented: " +
                                    qulifierType.getName());
                }
                this.annotations.add(AnnotationUtil.createAnnotation(qulifierType));
            }
            this.fireEvent = false;
            return this;
        }

        /**
         * Turn on `forceFireEvent` flag.
         *
         * Once force fire event flag is turned on, when it calls
         * {@link #register(Genie)} method the
         * {@link Genie#fireProviderRegisteredEvent(Class)} method
         * will be called
         *
         * @return this binder instance
         */
        public Binder<T> forceFireEvent() {
            this.forceFireEvent = true;
            this.fireEvent = true;
            return this;
        }

        /**
         * Turn off the `forceFireEvent` flag.
         *
         * If this flag is turned off, no {@link Genie#fireProviderRegisteredEvent(Class)}
         * call will happen upon {@link #register(Genie)} method is called
         *
         * @return this binder instance
         */
        public Binder<T> doNotFireEvent() {
            this.forceFireEvent = false;
            this.fireEvent = false;
            return this;
        }

        /**
         * Check if binding is setup
         *
         * @return `true` if binding is setup
         */
        boolean bound() {
            return null != provider || null != constructor;
        }

        /**
         * Register this binder to `Genie`
         *
         * @param genie the dependency injector
         */
        public void register(Genie genie) {
            if (null == provider) {
                if (null != constructor) {
                    provider = genie.buildConstructor(
                            constructor,
                            BeanSpec.of(constructor.getDeclaringClass(), null, genie),
                            new HashSet<BeanSpec>());
                } else if (null != impl) {
                    provider = new LazyProvider<>(impl, genie);
                }
            }
            if (!bound()) {
                throw new InjectException("Cannot register without binding specified");
            }
            BeanSpec spec = beanSpec(genie);
            genie.addIntoRegistry(spec, genie.decorate(spec, provider, true), annotations.isEmpty() && S.blank(name));
            if (fireEvent || forceFireEvent) {
                genie.fireProviderRegisteredEvent(type);
            }
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
        private final BeanSpec fieldSpec;
        private final Provider provider;

        FieldInjector(Field field, BeanSpec fieldSpec, Provider provider) {
            this.field = field;
            this.fieldSpec = fieldSpec;
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

    private ConcurrentMap<BeanSpec, Provider<?>> registry = new ConcurrentHashMap<>();
    private ConcurrentMap<Class, Provider> expressRegistry = new ConcurrentHashMap<>();
    private Set<Class<? extends Annotation>> qualifierRegistry = new HashSet<>();
    private Set<Class<? extends Annotation>> injectTagRegistry = new HashSet<>();
    private Map<Class<? extends Annotation>, Class<? extends Annotation>> scopeAliases = new HashMap<>();
    private Map<Class<? extends Annotation>, ScopeCache> scopeProviders = new HashMap<>();
    private ConcurrentMap<Class<? extends Annotation>, PostConstructProcessor<?>> postConstructProcessors =
            new ConcurrentHashMap<Class<? extends Annotation>, PostConstructProcessor<?>>();
    private ConcurrentMap<Class, BeanSpec> beanSpecLookup = new ConcurrentHashMap<>();
    private ConcurrentMap<Class, GenericTypedBeanLoader> genericTypedBeanLoaders = new ConcurrentHashMap<>();
    private List<InjectListener> listeners = new ArrayList<>();
    private boolean supportInjectionPoint = false;

    Genie(Object... modules) {
        init(false, modules);
    }

    Genie(boolean noPlugin, Object... modules) {
        init(noPlugin, modules);
    }

    private Genie(InjectListener listener, Object... modules) {
        this.listeners.add(listener);
        init(false, modules);
    }

    private void init(boolean noPlugin, Object... modules) {
        registerBuiltInProviders();
        if (!noPlugin) {
            registerBuiltInPlugins();
        }
        if (modules.length > 0) {
            List list = new ArrayList();
            for (Object module : modules) {
                if (module instanceof InjectListener) {
                    listeners.add((InjectListener) module);
                } else if (module instanceof Class) {
                    Class moduleClass = (Class) module;
                    if (InjectListener.class.isAssignableFrom(moduleClass)) {
                        listeners.add((InjectListener) $.newInstance(moduleClass));
                    }
                }
                list.add(module);
            }
            // register real modules after listener get registered
            for (Object module : list) {
                registerModule(module);
            }
        }
        initScopeAliases();
    }

    public void supportInjectionPoint(boolean enabled) {
        this.supportInjectionPoint = enabled;
    }

    @Override
    public <T> T get(Class<T> type) {
        return getProvider(type).get();
    }

    public <T> T get(BeanSpec beanSpec) {
        Provider provider = findProvider(beanSpec, C.<BeanSpec>empty());
        return (T) provider.get();
    }

    /**
     * Check if a type has already been registered with a binding already
     * @param type the class
     * @return `true` if the type has already been registered to Genie with a binding
     */
    public boolean hasProvider(Class<?> type) {
        return expressRegistry.containsKey(type);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        Provider provider = expressRegistry.get(type);
        if (null == provider) {
            if (type.isArray()) {
                provider = ArrayProvider.of(type, this);
                expressRegistry.putIfAbsent(type, provider);
                return (Provider<T>) provider;
            }
            BeanSpec spec = beanSpecOf(type);
            provider = findProvider(spec, C.<BeanSpec>empty());
            expressRegistry.putIfAbsent(type, provider);
        }
        return (Provider<T>) provider;
    }

    public <T> void registerProvider(Class<T> type, Provider<? extends T> provider) {
        registerProvider(type, provider, true);
    }

    private <T> void registerProvider(Class<T> type, Provider<? extends T> provider, boolean fireEvent) {
        AFFINITY.set(0);
        bindProviderToClass(type, provider, fireEvent);
    }

    public void registerQualifiers(Class<? extends Annotation>... qualifiers) {
        this.qualifierRegistry.addAll(C.listOf(qualifiers));
    }

    public void registerQualifiers(Collection<Class<? extends Annotation>> qualifiers) {
        this.qualifierRegistry.addAll(qualifiers);
    }

    public void registerInjectTag(Class<? extends Annotation>... injectTags) {
        this.injectTagRegistry.addAll(C.listOf(injectTags));
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

    public <T> void registerGenericTypedBeanLoader(Class<T> type, GenericTypedBeanLoader<T> loader) {
        genericTypedBeanLoaders.put(type, loader);
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annoClass) {
        if (Singleton.class == annoClass || SessionScoped.class == annoClass || RequestScoped.class == annoClass) {
            return true;
        }
        Class<? extends Annotation> mapped = scopeAliases.get(annoClass);
        return ((null != mapped && StopInheritedScope.class != mapped)) || scopeProviders.containsKey(annoClass);
    }

    @Override
    public boolean isInheritedScopeStopper(Class<? extends Annotation> annoClass) {
        if (StopInheritedScope.class == annoClass) {
            return true;
        }
        Class<? extends Annotation> mapped = scopeAliases.get(annoClass);
        return StopInheritedScope.class == mapped;
    }

    @Override
    public boolean isQualifier(Class<? extends Annotation> annoClass) {
        return qualifierRegistry.contains(annoClass) || annoClass.isAnnotationPresent(Qualifier.class);
    }

    @Override
    public boolean isPostConstructProcessor(Class<? extends Annotation> annoClass) {
        return postConstructProcessors.containsKey(annoClass) || annoClass.isAnnotationPresent(PostConstructProcess.class);
    }

    @Override
    public Class<? extends Annotation> scopeByAlias(Class<? extends Annotation> alias) {
        Class<? extends Annotation> annoType = scopeAliases.get(alias);
        return null == annoType ? alias : annoType;
    }

    ScopeCache scopeCache(Class<? extends Annotation> scope) {
        return scopeProviders.get(scope);
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

    private void bindProviderToClass(Class<?> target, Provider<?> provider, boolean fireEvent) {
        addIntoRegistry(target, provider);
        AFFINITY.set(AFFINITY.get() + 1);
        Class dad = target.getSuperclass();
        if (null != dad && Object.class != dad) {
            bindProviderToClass(dad, provider, fireEvent);
        }
        Class[] roles = target.getInterfaces();
        if (null == roles) {
            return;
        }
        for (Class role : roles) {
            bindProviderToClass(role, provider, fireEvent);
        }
        if (fireEvent) {
            fireProviderRegisteredEvent(target);
        }
    }

    private void addIntoRegistry(BeanSpec spec, Provider<?> val, boolean addIntoExpressRegistry) {
        WeightedProvider current = WeightedProvider.decorate(val);
        Provider<?> old = registry.get(spec);
        if (null == old) {
            registry.put(spec, current);
            if (addIntoExpressRegistry) {
                expressRegistry.put(spec.rawType(), current);
            }
            return;
        }
        String newName = providerName(current.realProvider);
        if (old instanceof WeightedProvider) {
            WeightedProvider weightedOld = (WeightedProvider) old;
            String oldName = providerName(weightedOld.realProvider);
            if (weightedOld.affinity > current.affinity) {
                registry.put(spec, current);
                if (addIntoExpressRegistry) {
                    expressRegistry.put(spec.rawType(), current);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Provider %s \n\tfor [%s] \n\tis replaced with: %s", oldName, spec, newName);
                }
            } else {
                if (weightedOld.affinity == 0 && current.affinity == 0) {
                    throw new InjectException("Provider has already registered for spec: %s", spec);
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Provider %s \n\t for [%s] \n\t cannot be replaced with: %s", oldName, spec, newName);
                    }
                }
            }
        } else {
            throw E.unexpected("Provider has already registered for spec: %s", spec);
        }
    }

    private void addIntoRegistry(Class<?> type, Provider<?> val) {
        addIntoRegistry(BeanSpec.of(type, this), val, true);
    }

    private void registerBuiltInProviders() {
        registerProvider(Collection.class, OsglListProvider.INSTANCE, false);
        registerProvider(Deque.class, DequeProvider.INSTANCE, false);
        registerProvider(ArrayList.class, ArrayListProvider.INSTANCE, false);
        registerProvider(LinkedList.class, LinkedListProvider.INSTANCE, false);
        registerProvider(C.List.class, OsglListProvider.INSTANCE, false);
        registerProvider(C.Set.class, OsglSetProvider.INSTANCE, false);
        registerProvider(C.Map.class, OsglMapProvider.INSTANCE, false);
        registerProvider(ConcurrentMap.class, ConcurrentMapProvider.INSTANCE, false);
        registerProvider(SortedMap.class, SortedMapProvider.INSTANCE, false);
        registerProvider(SortedSet.class, SortedSetProvider.INSTANCE, false);
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

    private void initScopeAliases() {
        scopeAliases.put(Singleton.class, Singleton.class);
        scopeAliases.put(SessionScoped.class, SessionScoped.class);
        scopeAliases.put(RequestScoped.class, RequestScoped.class);
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
        Annotation[] factoryAnnotations = factory.getAnnotations();
        final BeanSpec spec = BeanSpec.of(retType, factoryAnnotations, this);
        final MethodInjector methodInjector = methodInjector(factory, C.<BeanSpec>empty());
        addIntoRegistry(spec, decorate(spec, new Provider() {
            @Override
            public Object get() {
                return methodInjector.applyTo(instance);
            }

            @Override
            public String toString() {
                return S.fmt("%s::%s", instance.getClass().getName(), methodInjector.method.getName());
            }
        }, true), factoryAnnotations.length == 0);
        fireProviderRegisteredEvent(spec.rawType());
    }

    private Provider<?> findProvider(final BeanSpec spec, final Set<BeanSpec> chain) {

        // try registry
        Provider<?> provider = registry.get(spec);
        if (null != provider) {
            return provider;
        }

        // try without name
        if (null != spec.name()) {
            provider = registry.get(spec.withoutName());
            if (null != provider) {
                return provider;
            }
        }

        // does it want to inject a Provider?
        if (spec.isProvider() && !spec.typeParams().isEmpty()) {
            provider = new Provider<Provider<?>>() {
                @Override
                public Provider<?> get() {
                    return new Provider() {
                        private volatile Provider realProvider;
                        @Override
                        public Object get() {
                            if (null == realProvider) {
                                synchronized (this) {
                                    if (null == realProvider) {
                                        realProvider = findProvider(spec.toProvidee(), C.<BeanSpec>empty());
                                    }
                                }
                            }
                            return realProvider.get();
                        }
                    };
                }
            };
            registry.putIfAbsent(spec, provider);
            return provider;
        }

        // does it require a value loading logic
        if (spec.hasValueLoader()) {
            provider = ValueLoaderFactory.create(spec, this);
        } else {
            // does it require an array
            if (spec.isArray()) {
                return ArrayProvider.of(spec, this);
            }
            // check if there is a generic typed bean loader
            final GenericTypedBeanLoader loader = genericTypedBeanLoaders.get(spec.rawType());
            if (null != loader) {
                provider = new Provider<Object>() {
                    @Override
                    public Object get() {
                        return loader.load(spec);
                    }
                };
            }
            if (null == provider) {
                // build provider from constructor, field or method
                if (spec.notConstructable()) {
                    // does spec's bare class have provider?
                    provider = registry.get(spec.rawTypeSpec());
                    if (null == provider) {
                        throw new InjectException("Cannot instantiate %s", spec);
                    }
                } else {
                    if (BeanSpec.class == spec.rawType()) {
                        return BEAN_SPEC_PROVIDER;
                    }
                    provider = buildProvider(spec, chain);
                }
            }
        }
        Provider<?> decorated = decorate(spec, provider, false);
        registry.putIfAbsent(spec, decorated);
        return decorated;
    }


    private Provider<?> decorate(final BeanSpec spec, Provider provider, final boolean isFactory) {
        if (BEAN_SPEC_PROVIDER == provider) {
            return provider;
        }
        final Provider postConstructed = PostConstructProcessorInvoker.decorate(spec,
                PostConstructorInvoker.decorate(spec,
                        ElementLoaderProvider.decorate(spec, provider, this),
                        this),
                this);
        Provider eventFired = new Provider() {
            @Override
            public Object get() {
                if (supportInjectionPoint && !isFactory) {
                    TGT_SPEC.set(spec);
                }
                try {
                    Object bean = postConstructed.get();
                    fireInjectEvent(bean, spec);
                    return bean;
                } finally {
                    if (supportInjectionPoint && !isFactory) {
                        TGT_SPEC.remove();
                    }
                }
            }
        };
        return ScopedProvider.decorate(spec, eventFired, this);
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
                            if (supportInjectionPoint) {
                                TGT_SPEC.set(fj.fieldSpec);
                            }
                            try {
                                fj.applyTo(bean);
                            } finally {
                                if (supportInjectionPoint) {
                                    TGT_SPEC.remove();
                                }
                            }
                        }
                        for (MethodInjector mj : methodInjectors) {
                            mj.applyTo(bean);
                        }
                        return bean;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getTargetException();
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        }
                        throw new InjectException(t, "cannot instantiate %s", spec);
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
            if (subjectToInject(c)) {
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
                if (subjectToInject(field)) {
                    field.setAccessible(true);
                    fieldInjectors.add(fieldInjector(field, chain));
                }
            }
            current = current.getSuperclass();
        }
        return fieldInjectors;
    }

    private FieldInjector fieldInjector(Field field, Set<BeanSpec> chain) {
        BeanSpec fieldSpec = BeanSpec.of(field, this);
        if (chain.contains(fieldSpec)) {
            foundCircularDependency(chain, fieldSpec);
        }
        return new FieldInjector(field, fieldSpec, findProvider(fieldSpec, chain(chain, fieldSpec)));
    }

    private List<MethodInjector> methodInjectors(Class type, Set<BeanSpec> chain) {
        Class<?> current = type;
        List<MethodInjector> methodInjectors = C.newList();
        while (null != current && !current.equals(Object.class)) {
            for (Method method : current.getDeclaredMethods()) {
                if (subjectToInject(method)) {
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
                        provider = ValueLoaderFactory.create(paramSpec, this);
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

    private void fireProviderRegisteredEvent(Class targetType) {
        for (InjectListener l : listeners) {
            l.providerRegistered(targetType);
        }
    }

    void fireInjectEvent(Object bean, BeanSpec beanSpec) {
        for (InjectListener l : listeners) {
            l.injected(bean, beanSpec);
        }
    }

    public boolean subjectToInject(AccessibleObject ao) {
        if (ao.isAnnotationPresent(Inject.class)) {
            return true;
        }
        for (Class<? extends Annotation> tag : injectTagRegistry) {
            if (ao.isAnnotationPresent(tag)) {
                return true;
            }
        }
        for (Annotation tag : ao.getDeclaredAnnotations()) {
            Class<? extends Annotation> tagType = tag.annotationType();
            if (tagType.isAnnotationPresent(InjectTag.class)) {
                injectTagRegistry.add(tagType);
                return true;
            }
        }
        return false;
    }

    public boolean subjectToInject(BeanSpec beanSpec) {
        if (registry.containsKey(beanSpec)) {
            return true;
        }
        if (beanSpec.hasAnnotation(Inject.class)) {
            return true;
        }
        for (Class<? extends Annotation> tag : injectTagRegistry) {
            if (beanSpec.hasAnnotation(tag)) {
                return true;
            }
        }
        for (Annotation tag : beanSpec.allAnnotations()) {
            Class<? extends Annotation> tagType = tag.annotationType();
            if (tagType.isAnnotationPresent(InjectTag.class)) {
                injectTagRegistry.add(tagType);
                return true;
            }
        }
        return false;
    }


    public static Genie create(InjectListener listener, Object... modules) {
        return new Genie(listener, modules);
    }

    /**
     * Create a Genie instance with modules specified
     *
     * @param modules modules that provides binding or @Provides methods
     * @return an new Genie instance with modules
     */
    public static Genie create(Object... modules) {
        return new Genie(modules);
    }

    /**
     * Create a Genie instance with modules specified
     *
     * @param modules modules that provides binding or @Provides methods
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

    private static S.Buffer debugChain(Set<BeanSpec> chain, BeanSpec last) {
        S.Buffer sb = S.buffer();
        for (BeanSpec spec : chain) {
            sb.append(spec).append(" -> ");
        }
        sb.append(last);
        return sb;
    }

    private static void foundCircularDependency(Set<BeanSpec> chain, BeanSpec last) {
        throw InjectException.circularDependency(debugChain(chain, last));
    }

    private static String providerName(Provider provider) {
        String name = provider.getClass().getName();
        if (name.contains("org.osgl.inject.Genie$")) {
            name = provider.toString();
        }
        return name;
    }

}
