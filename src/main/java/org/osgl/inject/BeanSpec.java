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
import org.osgl.inject.annotation.*;
import org.osgl.inject.util.AnnotationUtil;
import org.osgl.inject.util.ParameterizedTypeImpl;
import org.osgl.util.AnnotationAware;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Specification of a bean to be injected.
 */
public class BeanSpec implements AnnotationAware {

    /**
     * The dependency injector.
     *
     * The injector can be used to do some check including
     * the following:
     *
     * * {@link Injector#isQualifier(Class)}
     * * {@link Injector#scopeByAlias(Class)}
     */
    private final Injector injector;

    /**
     * Pre-calculated {@link Object#hashCode()} of this bean spec.
     */
    private final int hc;

    /**
     * The {@link Type} of the bean.
     */
    private final Type type;

    /**
     * The raw type of {@link #type()}
     */
    private transient volatile Class<?> rawType;

    /**
     * The origin of this BeanSpec
     */
    private Field field;

    /**
     * Is the bean an array type or not.
     */
    private final boolean isArray;

    private final Set<Annotation> elementLoaders = new HashSet<>();
    private final Set<Annotation> filters = new HashSet<>();
    private final Set<Annotation> transformers = new HashSet<>();
    private final Set<Annotation> qualifiers = new HashSet<>();
    private final Set<Annotation> postProcessors = new HashSet<>();
    // only applied when bean spec constructed from Field
    private final int modifiers;

    /**
     * The annotations will be used for calculating the hashCode and do
     * equality test. The following annotations will added into
     * the list:
     * * {@link #elementLoaders}
     * * {@link #filters}
     * * {@link #qualifiers}
     * * {@link #valueLoader}
     * * {@link #postProcessors}
     */
    private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

    /**
     * Stores all annotations including the ones that participating
     * hashCode calculating and those who don't equality test.
     *
     * @see #annotations
     */
    private final Map<Class<? extends Annotation>, Annotation> allAnnotations = new HashMap<>();

    /**
     * Stores all annotations that are tagged with another annotation mapped to the tag annotation class.
     * E.g. for all annotation that are marked with `@Qualifier` will be stored in a set mapped to
     * `Qualifier.class`
     */
    private final Map<Class<? extends Annotation>, Set<Annotation>> tagAnnotations = new HashMap<>();

    private final Set<AnnoData> annoData = new HashSet<>();
    private final Set<AnnoData> injectTags = new HashSet<>();

    /**
     * Store the name value of Named annotation if presented.
     */
    private String originalName;
    private String name;
    private MapKey mapKey;
    private Class<? extends Annotation> scope;
    private BeanSpec componentSpec;
    private volatile boolean componentSpecSet;
    private boolean stopInheritedScope;
    private Annotation valueLoader;
    private List<Type> typeParams;
    // simple type without injection tag
    private volatile Map<String, BeanSpec> fields;

    /**
     * Construct the `BeanSpec` with bean type and field or parameter
     * annotations.
     *
     * @param type
     *      the type of the bean to be instantiated
     * @param annotations
     *      the annotation tagged on field or parameter,
     *      or `null` if this is a direct API injection
     *      request
     * @param name
     *      optional, the name coming from the Named qualifier
     * @param injector
     *      the injector instance
     * @param modifiers
     *      the modifiers
     */
    private BeanSpec(Type type, Annotation[] annotations, String name, Injector injector, int modifiers) {
        this.injector = injector;
        this.type = type;
        this.originalName = name;
        this.name = name;
        Class<?> rawType = rawType();
        this.isArray = rawType.isArray();
        this.resolveTypeAnnotations(injector);
        this.resolveAnnotations(annotations, injector);
        this.hc = calcHashCode();
        this.modifiers = modifiers;
    }

    private BeanSpec(BeanSpec source, Type convertTo) {
        this.originalName = source.name;
        this.name = source.name;
        this.injector = source.injector;
        this.type = convertTo;
        this.isArray = rawType().isArray();
        this.qualifiers.addAll(source.qualifiers);
        this.elementLoaders.addAll(source.elementLoaders);
        this.filters.addAll(source.filters);
        this.transformers.addAll(source.transformers);
        this.valueLoader = source.valueLoader;
        this.annotations.putAll(source.annotations);
        this.annoData.addAll(source.annoData);
        this.allAnnotations.putAll(source.allAnnotations);
        this.tagAnnotations.putAll(source.tagAnnotations);
        this.hc = calcHashCode();
        this.modifiers = source.modifiers;
    }

    private BeanSpec(BeanSpec source, String name) {
        this.originalName = source.name;
        this.name = name;
        this.injector = source.injector;
        this.type = source.type;
        this.isArray = rawType().isArray();
        this.qualifiers.addAll(source.qualifiers);
        this.elementLoaders.addAll(source.elementLoaders);
        this.filters.addAll(source.filters);
        this.transformers.addAll(source.transformers);
        this.valueLoader = source.valueLoader;
        this.annotations.putAll(source.annotations);
        this.annoData.addAll(source.annoData);
        this.allAnnotations.putAll(source.allAnnotations);
        this.tagAnnotations.putAll(source.tagAnnotations);
        this.hc = calcHashCode();
        this.modifiers = source.modifiers;
    }

    @Override
    public int hashCode() {
        return hc;
    }

    /**
     * A bean spec equals to another bean spec if all of the following conditions are met:
     * * the {@link #type} of the two bean spec equals to each other
     * * the {@link #annotations} of the two bean spec equals to each other
     *
     * Specifically, {@link #scope} does not participate comparison because
     * 1. Scope annotations shall be put onto {@link java.lang.annotation.ElementType#TYPE type},
     *    or the factory method with {@link Provides} annotation, which is equivalent to `Type`.
     *    So it is safe to ignore scope annotation because one type cannot be annotated with different
     *    scope
     * 2. If we count scope annotation in equality test, we will never be able to get the correct
     *    provider stem from the factory methods.
     *
     * @param obj the object to compare with this object
     * @return `true` if the two objects equals as per described above
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BeanSpec) {
            BeanSpec that = (BeanSpec) obj;
            return that.hc == hc
                    && $.eq(type, that.type)
                    && $.eq(name, that.name)
                    && $.eq(annoData, that.annoData);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = S.builder(type());
        if (S.notBlank(name)) {
            sb.append("(").append(name).append(")");
        }
        C.List<Object> list = C.newList();
        if (null != valueLoader) {
            list.append(valueLoader);
        } else {
            list.append(qualifiers).append(elementLoaders).append(filters);
            if (null != mapKey) {
                list.append(mapKey);
            }
        }
        if (null != scope) {
            list.append(scope.getSimpleName());
        }
        if (!list.isEmpty()) {
            sb.append("@[").append(S.join(", ", list)).append("]");
        }
        return sb.toString();
    }

    public Injector injector() {
        return injector;
    }

    public Type type() {
        return type;
    }

    public Class rawType() {
        if (null == rawType) {
            synchronized (this) {
                if (null == rawType) {
                    rawType = rawTypeOf(type);
                }
            }
        }
        return rawType;
    }

    public String name() {
        return name;
    }

    public boolean isArray() {
        return isArray;
    }

    public Annotation[] allAnnotations() {
        return allAnnotations.values().toArray(new Annotation[allAnnotations.size()]);
    }

    public Annotation[] taggedAnnotations(Class<? extends Annotation> tagType) {
        Set<Annotation> tagged = tagAnnotations.get(tagType);
        return null == tagged ? new Annotation[0] : tagged.toArray(new Annotation[tagged.size()]);
    }

    /**
     * Convert an array bean spec to a list bean spec.
     *
     * @return
     *      the array bean spec with component type derived from this bean spec
     */
    public BeanSpec toList() {
        return new BeanSpec(this, ArrayList.class);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annoClass) {
        return (T)allAnnotations.get(annoClass);
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annoClass) {
        return allAnnotations.containsKey(annoClass);
    }

    public boolean hasAnnotation() {
        return !allAnnotations.isEmpty();
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public boolean isInterface() {
        return rawType().isInterface();
    }

    BeanSpec rawTypeSpec() {
        return BeanSpec.of(rawType(), injector);
    }

    boolean isMap() {
        return Map.class.isAssignableFrom(rawType());
    }

    MapKey mapKey() {
        return mapKey;
    }

    boolean isProvider() {
        return Provider.class.isAssignableFrom(rawType());
    }

    BeanSpec toProvidee() {
        return new BeanSpec(this, ((ParameterizedType) type).getActualTypeArguments()[0]);
    }

    public BeanSpec withoutName() {
        return new BeanSpec(this, (String) null);
    }

    BeanSpec withoutQualifiers() {
        if (qualifiers.isEmpty()) {
            return this;
        }
        BeanSpec spec = withoutName();
        spec.qualifiers.clear();
        return spec;
    }

    /**
     * Returns a list of type parameters if the
     * {@link #type()} of the bean is instance of
     * {@link ParameterizedType}.
     *
     * For example the bean spec of a field declared
     * as
     *
     * ```java
     * private Map<String, Integer> scores;
     * ```
     *
     * The `typeParams()` method will return a list
     * of `String` and `Integer`
     *
     * @return type parameter list
     */
    public List<Type> typeParams() {
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

    public BeanSpec componentSpec() {
        if (!componentSpecSet) {
            synchronized (this) {
                if (!componentSpecSet) {
                    componentSpecSet = true;
                    if (isArray()) {
                        componentSpec = BeanSpec.of(rawType.getComponentType(), injector);
                    } else {
                        List<Type> typeParams = typeParams();
                        if (!typeParams.isEmpty()) {
                            componentSpec = BeanSpec.of(typeParams.get(0), injector);
                        }
                    }
                }
            }
        }
        return componentSpec;
    }

    /**
     * Return if the bean is instance of give class.
     *
     * @param c
     *      the class
     * @return
     *      `true` if the underline bean is an instance of `c`
     */
    public boolean isInstanceOf(Class c) {
        return c.isAssignableFrom(rawType());
    }

    /**
     * Check if a given object is instance of the type of this bean spec.
     *
     * @param o
     *      the object instance
     * @return
     *      `true` if the object is an instance of type of this bean spec
     */
    public boolean isInstance(Object o) {
        Class c = rawType();
        if (c.isInstance(o))  {
            return true;
        }
        Class p = $.primitiveTypeOf(c);
        if (null != p && p.isInstance(o)) {
            return true;
        }
        Class w = $.wrapperClassOf(c);
        if (null != w && w.isInstance(o)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the bean spec has injector decorators. The following annotations
     * are considered to be inject decorators:
     * * qualifiers
     * * post construct annotation
     * * scope annotation
     * * {@link Inject} or any annotation has {@link InjectTag} presented
     * @return `true` if the beanSpec has inject decorators or `false` otherwise
     */
    public boolean hasInjectDecorator() {
        return !annoData.isEmpty() || !injectTags.isEmpty();
    }

    /**
     * Returns all qualifier annotation of this bean spec.
     *
     * @return
     *      all qualifier annotations of this bean spec
     */
    public Set<Annotation> qualifiers() {
        return new HashSet<>(qualifiers);
    }

    public BeanSpec parent() {
        return BeanSpec.of(rawType().getGenericSuperclass(), injector());
    }

    public boolean isObject() {
        return Object.class == rawType();
    }

    public boolean isNotObject() {
        return Object.class != rawType();
    }

    Constructor getDeclaredConstructor() {
        try {
            return rawType().getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new InjectException(e, "cannot instantiate %s", rawType);
        }
    }

    Method[] getDeclaredMethods() {
        return rawType().getDeclaredMethods();
    }

    public BeanSpec field(String name) {
        return fields().get(name);
    }

    /**
     * Returns a list of `BeanSpec` for all fields including super class fields.
     * @return the fields spec list
     */
    public Map<String, BeanSpec> fields() {
        if (null != fields) {
            return fields;
        }
        synchronized (this) {
            if (null == fields) {
                Map<String, BeanSpec> map = new HashMap<>();
                for (BeanSpec spec: fields($.F.<Field>yes())) {
                    map.put(spec.name, spec);
                    // need to treat the alias of field name
                    if (spec.originalName != spec.name) {
                        map.put(spec.originalName, spec);
                    }
                }
                fields = Collections.unmodifiableMap(map);
            }
        }
        return fields;
    }

    public List<BeanSpec> nonStaticFields() {
        return fields(NON_STATIC_FIELD);
    }

    /**
     * Returns a list of `BeanSpec` for all fields including super class fields.
     * @param filter a function to filter out fields that are not needed.
     * @return the fields spec list
     */
    public List<BeanSpec> fields($.Predicate<Field> filter) {
        List<BeanSpec> retVal = new ArrayList<>();
        BeanSpec current = this;
        Type[] typeDeclarations = rawType().getTypeParameters();
        while (null != current && current.isNotObject()) {
            Type[] fieldTypeParams = null;
            Type[] classTypeParams;
            Map<String, Type> typeParamsMapping = new HashMap<>();
            for (Field field : current.rawType().getDeclaredFields()) {
                if (!filter.test(field)) {
                    continue;
                }
                Type fieldGenericType = field.getGenericType();
                if (fieldGenericType instanceof ParameterizedType) {
                    if (null == fieldTypeParams) {
                        fieldTypeParams = ((ParameterizedType) fieldGenericType).getActualTypeArguments();
                        if (fieldTypeParams != null && fieldTypeParams.length > 0) {
                            classTypeParams = current.rawType().getTypeParameters();
                            if (classTypeParams != null && classTypeParams.length > 0) {
                                List<Type> classTypeImpls = current.typeParams();
                                for (int i = 0; i < classTypeParams.length; ++i) {
                                    if (i >= classTypeImpls.size()) {
                                        break;
                                    }
                                    Type classTypeParam = classTypeParams[i];
                                    if (classTypeParam instanceof TypeVariable) {
                                        typeParamsMapping.put(((TypeVariable) classTypeParam).getName(), classTypeImpls.get(i));
                                    }
                                }
                            }
                        }
                    }
                    boolean updated = false;
                    if (!typeParamsMapping.isEmpty()) {
                        for (int i = 0; i < fieldTypeParams.length; ++i) {
                            Type typeArg = fieldTypeParams[i];
                            if (typeArg instanceof TypeVariable) {
                                String name = ((TypeVariable) typeArg).getName();
                                Type typeImpl = typeParamsMapping.get(name);
                                if (null != typeImpl && $.ne(typeImpl, typeArg)) {
                                    updated = true;
                                    fieldTypeParams[i] = typeImpl;
                                }
                            }
                        }
                    }
                    if (updated) {
                        fieldGenericType = new ParameterizedTypeImpl(fieldTypeParams, ((ParameterizedType) fieldGenericType).getOwnerType(), ((ParameterizedType) fieldGenericType).getRawType());
                    }
                    retVal.add(beanSpecOf(field, fieldGenericType));
                } else if (fieldGenericType instanceof TypeVariable) {
                    boolean added = false;
                    for (int i = typeDeclarations.length - 1; i >= 0; --i) {
                        if (typeDeclarations[i].equals(fieldGenericType)) {
                            fieldGenericType = typeParams().get(i);
                            retVal.add(beanSpecOf(field, fieldGenericType));
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        throw new InjectException("Cannot infer field type: " + field);
                    }
                } else {
                    retVal.add(of(field, injector()));
                }
            }
            current = current.parent();
        }
        return retVal;
    }

    // find the beanspec of a field with generic type
    private BeanSpec beanSpecOf(Field field, Type genericType) {
        return of(field, genericType, injector);
//        int len = typeArgs.size();
//        for (int i = 0; i < len; ++i) {
//            if (genericType.equals(typeArgs.get(i))) {
//                return of(field, typeImpls.get(i), injector());
//            }
//        }
//        return of(field, injector);
    }

    boolean hasElementLoader() {
        return !elementLoaders.isEmpty();
    }

    boolean hasValueLoader() {
        return null != valueLoader;
    }

    Set<Annotation> loaders() {
        return elementLoaders;
    }

    Set<Annotation> filters() {
        return filters;
    }

    Set<Annotation> transformers() {
        return transformers;
    }

    Set<Annotation> postProcessors() {
        return postProcessors;
    }

    Annotation valueLoader() {
        return valueLoader;
    }

    Class<? extends Annotation> scope() {
        return scope;
    }

    BeanSpec scope(Class<? extends Annotation> scopeAnno) {
        scope = scopeAnno;
        return this;
    }

    void makeFieldAccessible() {
        E.illegalStateIf(null == field);
        field.setAccessible(true);
    }

    void setField(Object bean, Object value) {
        E.illegalStateIf(null == field);
        try {
            field.set(bean, value);
        } catch (Exception e) {
            throw new InjectException(e, "Unable to inject field value on %s", bean.getClass());
        }

    }

    boolean notConstructable() {
        Class<?> c = rawType();
        return c.isInterface() || c.isArray() || Modifier.isAbstract(c.getModifiers());
    }

    private void resolveTypeAnnotations(Injector injector) {
        for (Annotation annotation : rawType().getAnnotations()) {
            resolveScope(annotation, injector);
            Class<? extends Annotation> annoType = annotation.annotationType();
            if (annoType == Named.class) {
                name = ((Named)annotation).value();
            } else if (injector.isQualifier(annoType)) {
                qualifiers.add(annotation);
            }
            allAnnotations.put(annotation.annotationType(), annotation);
            storeTagAnnotation(annotation);
        }
    }

    private void resolveAnnotations(Annotation[] aa, Injector injector) {
        if (null == aa || aa.length == 0) {
            return;
        }
        Class<?> rawType = rawType();
        boolean isMap = Map.class.isAssignableFrom(rawType);
        boolean isContainer = isMap || Collection.class.isAssignableFrom(rawType) || rawType.isArray();
        MapKey mapKey = null;
        List<Annotation> loadValueIncompatibles = new ArrayList<Annotation>();
        // Note only qualifiers and bean loaders annotation are considered
        // effective annotation. Scope annotations is not effective here
        // because they are tagged on target type, not the field or method
        // parameter
        for (Annotation anno : aa) {
            Class<? extends Annotation> cls = anno.annotationType();
            Annotation prev = allAnnotations.put(cls, anno);
            if (null == prev || anno != prev) {
                storeTagAnnotation(anno);
            }
            if (Inject.class == cls || Provides.class == cls) {
                injectTags.add(new AnnoData(anno));
                continue;
            }
            boolean isInjectTag = tagAnnotations.containsKey(InjectTag.class);
            if (isInjectTag) {
                injectTags.add(new AnnoData(anno));
            }
            if (cls == MapKey.class) {
                if (null != mapKey) {
                    throw new InjectException("MapKey annotation already presented");
                }
                if (!isMap) {
                    Genie.logger.warn("MapKey annotation ignored on target that is not of Map type");
                } else {
                    mapKey = $.cast(anno);
                }
            }
            if (LoadValue.class == cls || cls.isAnnotationPresent(LoadValue.class)) {
                valueLoader = anno;
            } else if (LoadCollection.class == cls || cls.isAnnotationPresent(LoadCollection.class)) {
                if (isContainer) {
                    elementLoaders.add(anno);
                    loadValueIncompatibles.add(anno);
                } else {
                    Genie.logger.warn("LoadCollection annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                }
            } else if (Named.class == cls) {
                name = ((Named)anno).value();
            } else if (injector.isQualifier(cls)) {
                qualifiers.add(anno);
                loadValueIncompatibles.add(anno);
            } else if (Filter.class == cls || cls.isAnnotationPresent(Filter.class)) {
                if (isContainer) {
                    filters.add(anno);
                    loadValueIncompatibles.add(anno);
                } else {
                    Genie.logger.warn("Filter annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                }
            } else if (Transform.class == cls || cls.isAnnotationPresent(Transform.class)) {
                transformers.add(anno);
            } else {
                if (injector.isPostConstructProcessor(cls)) {
                    postProcessors.add(anno);
                    annotations.put(cls, anno);
                    //annoData.add(new AnnoData(anno));
                } else {
                    resolveScope(anno, injector);
                }
            }
        }
        if (isMap && hasElementLoader() && null == mapKey) {
            throw new InjectException("No MapKey annotation found on Map type target with ElementLoader annotation presented");
        }
        if (isContainer && null == valueLoader && elementLoaders.isEmpty()) {
            // assume we want to inject typed elements
            Class<?> rawType0;
            if (rawType.isArray()) {
                rawType0 = rawType.getComponentType();
            } else {
                List<Type> typeParams = typeParams();
                if (typeParams.isEmpty()) {
                    rawType0 = Object.class;
                } else {
                    Type theType = typeParams.get(isMap ? 1 : 0);
                    rawType0 = rawTypeOf(theType);
                }
            }
            if (!$.isSimpleType(rawType0)) {
                TypeOf typeOfAnno = AnnotationUtil.createAnnotation(TypeOf.class);
                elementLoaders.add(typeOfAnno);
                loadValueIncompatibles.add(typeOfAnno);
            }
        }
        if (null != valueLoader) {
            if (!loadValueIncompatibles.isEmpty()) {
                throw new InjectException("ValueLoader annotation cannot be used with ElementLoader and Filter annotations: %s", annotations);
            }
            annotations.put(valueLoader.annotationType(), valueLoader);
            annoData.add(new AnnoData(valueLoader));
        } else {
            for (Annotation anno: loadValueIncompatibles) {
                annotations.put(anno.annotationType(), anno);
                annoData.add(new AnnoData(anno));
            }
            if (null != mapKey) {
                if (hasElementLoader()) {
                    this.mapKey = mapKey;
                    annotations.put(mapKey.annotationType(), mapKey);
                    annoData.add(new AnnoData(mapKey));
                } else {
                    Genie.logger.warn("MapKey annotation ignored on target without ElementLoader annotation presented");
                }
            }
        }
    }

    private static Set<Class<? extends Annotation>> WAIVE_TAG_TYPES = C.set(
            Documented.class, Retention.class, Target.class, Inherited.class
    );

    /**
     * Walk through anno's tag annotations
     * @param anno the annotation
     */
    private void storeTagAnnotation(Annotation anno) {
        Class<? extends Annotation> annoType = anno.annotationType();
        Annotation[] tags = annoType.getAnnotations();
        for (Annotation tag : tags) {
            Class<? extends Annotation> tagType = tag.annotationType();
            if (WAIVE_TAG_TYPES.contains(tagType)) {
                continue;
            }
            Set<Annotation> tagged = tagAnnotations.get(tagType);
            if (null == tagged) {
                tagged = new HashSet<>();
                tagAnnotations.put(tagType, tagged);
            }
            tagged.add(anno);
        }
    }

    private void resolveScope(Annotation annotation, Injector injector) {
        if (stopInheritedScope) {
            return;
        }
        Class<? extends Annotation> annoClass = annotation.annotationType();
        if (injector.isInheritedScopeStopper(annoClass)) {
            stopInheritedScope = true;
            scope = null;
        } else if (injector.isScope(annoClass)) {
            if (null != scope) {
                Class<? extends Annotation> newScope = injector.scopeByAlias(annoClass);
                if (newScope != scope) {
                    throw new InjectException("Incompatible scope annotation found: %s", this);
                }
            } else {
                scope = injector.scopeByAlias(annoClass);
                // scope annotaton is a decorator for inject library usage, it
                // can't add scope annotation into annoData and make it
                // enter the equals and hashCode calculation,
                //annoData.add(new AnnoData(annotation));
            }
        }
    }

    /**
     * Return hash code based on {@link #type} and {@link #annotations}.
     *
     * @return {@link Object#hashCode()} of this bean
     * @see #equals(Object)
     */
    private int calcHashCode() {
        return $.hc(type, name, annoData);
    }

    private BeanSpec setField(Field field) {
        this.field = field;
        return this;
    }

    public static BeanSpec of(Class<?> clazz, Injector injector) {
        return new BeanSpec(clazz, null, null, injector, 0);
    }

    public static BeanSpec of(Type type, Injector injector) {
        return new BeanSpec(type, null, null, injector, 0);
    }

    public static BeanSpec of(Type type, Annotation[] paramAnnotations, Injector injector) {
        return new BeanSpec(type, paramAnnotations, null, injector, 0);
    }

    public static BeanSpec of(Type type, Annotation[] paramAnnotations, Injector injector, int modifiers) {
        return new BeanSpec(type, paramAnnotations, null, injector, modifiers);
    }

    public static BeanSpec of(Type type, Annotation[] paramAnnotations, String name, Injector injector) {
        return new BeanSpec(type, paramAnnotations, name, injector, 0);
    }

    public static BeanSpec of(Type type, Annotation[] paramAnnotations, String name, Injector injector, int modifiers) {
        return new BeanSpec(type, paramAnnotations, name, injector, modifiers);
    }

    public static BeanSpec of(Field field, Injector injector) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        Type fieldType = field.getGenericType();
        if (fieldType instanceof TypeVariable) {
            fieldType = field.getType();
        }
        return BeanSpec.of(fieldType, annotations, field.getName(), injector, field.getModifiers()).setField(field);
    }

    private static BeanSpec of(Field field, Type realType, Injector injector) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        return of(realType, annotations, field.getName(), injector, field.getModifiers()).setField(field);
    }

    /**
     * A utility method to return raw type (the class) of a given
     * type.
     *
     * @param type
     *      a {@link Type}
     * @return
     *      a {@link Class} of the type
     * @throws org.osgl.exception.UnexpectedException
     *      if class cannot be determined
     */
    public static Class<?> rawTypeOf(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else {
            throw E.unexpected("type not recognized: %s", type);
        }
    }

    // keep the effective annotation data
    // - the property annotated with NonBinding is ignored
    private static class AnnoData {
        private Class<? extends Annotation> annoClass;
        private Map<String, Object> data;

        AnnoData(Annotation annotation) {
            annoClass = annotation.annotationType();
            data = evaluate(annotation);
        }

        @Override
        public int hashCode() {
            return $.hc(annoClass, data);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof AnnoData) {
                AnnoData that = (AnnoData) obj;
                return $.eq(annoClass, that.annoClass) && $.eq(data, that.data);
            }
            return false;
        }

        private static Map<String, Object> evaluate(Annotation anno) {
            Map<String, Object> properties = new HashMap<>();
            Class<? extends Annotation> annoClass = anno.annotationType();
            Method[] ma = annoClass.getMethods();
            for (Method m : ma) {
                if (isStandardAnnotationMethod(m) || shouldIgnore(m)) {
                    continue;
                }
                properties.put(m.getName(), $.invokeVirtual(anno, m));
            }
            return properties;
        }

        private static Set<String> standardsAnnotationMethods = C.set(C.list("equals", "hashCode", "toString", "annotationType", "getClass"));

        private static boolean isStandardAnnotationMethod(Method m) {
            return standardsAnnotationMethods.contains(m.getName());
        }

        private static boolean shouldIgnore(Method method) {
            Annotation[] aa = method.getDeclaredAnnotations();
            for (Annotation a: aa) {
                if (shouldIgnore(a)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean shouldIgnore(Annotation annotation) {
            return annotation.annotationType().getName().endsWith("Nonbinding");
        }
    }

    private static $.Predicate<Field> NON_STATIC_FIELD = new $.Predicate<Field>() {
        @Override
        public boolean test(Field field) {
            return !Modifier.isStatic(field.getModifiers());
        }
    };

}
