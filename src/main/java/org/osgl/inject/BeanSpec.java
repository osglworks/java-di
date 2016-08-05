package org.osgl.inject;

import org.osgl.$;
import org.osgl.inject.annotation.*;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Specification of a bean to be injected
 */
public class BeanSpec {
    private final Genie genie;
    private final int hc;
    private final Type type;
    private final boolean isArray;
    private final Set<Annotation> elementLoaders = C.newSet();
    private final Set<Annotation> filters = C.newSet();
    private final Set<Annotation> qualifiers = C.newSet();
    private final Set<Annotation> postProcessors = C.newSet();
    /**
     * The list will be used for calculating the hashCode and do
     * equality test. The following annotations will added into
     * the list:
     * * {@link #elementLoaders}
     * * {@link #filters}
     * * {@link #qualifiers}
     * * {@link #valueLoader}
     * * {@link #postProcessors}
     */
    private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
    /**
     * Store the name value of Named annotation if presented
     */
    private String name;
    private MapKey mapKey;
    private Class<? extends Annotation> scope;
    private Annotation valueLoader;
    private List<Type> typeParams;

    /**
     * Construct the `BeanSpec` with bean type and field or parameter
     * annotations
     *
     * @param type        the type of the bean to be instantiated
     * @param annotations the annotation tagged on field or parameter,
     *                    or `null` if this is a direct API injection
     *                    request
     */
    private BeanSpec(Type type, Annotation[] annotations, Genie genie) {
        this.genie = genie;
        this.type = type;
        this.isArray = rawType().isArray();
        this.resolveTypeAnnotations();
        this.resolveAnnotations(annotations);
        this.hc = calcHashCode();
    }

    private BeanSpec(BeanSpec source, Type convertTo) {
        this.genie = source.genie;
        this.type = convertTo;
        this.isArray = rawType().isArray();
        this.qualifiers.addAll(source.qualifiers);
        this.elementLoaders.addAll(source.elementLoaders);
        this.filters.addAll(source.filters);
        this.valueLoader = source.valueLoader;
        this.annotations.putAll(source.annotations);
        this.hc = calcHashCode();
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
                    && $.eq(annotations, that.annotations);
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

    public Class rawType() {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else {
            throw E.unexpected("type not recognized: %s", type);
        }
    }

    public String name() {
        return name;
    }

    public boolean isArray() {
        return isArray;
    }

    /**
     * Convert an array bean spec to a list bean spec
     * @return
     */
    public BeanSpec toList() {
        return new BeanSpec(this, ArrayList.class);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annoClass) {
        return (T)annotations.get(annoClass);
    }

    BeanSpec rawTypeSpec() {
        return BeanSpec.of(rawType(), genie);
    }

    Type type() {
        return type;
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

    boolean notConstructable() {
        Class<?> c = rawType();
        return c.isInterface() || c.isArray() || Modifier.isAbstract(c.getModifiers());
    }

    private void resolveTypeAnnotations() {
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
        boolean isContainer = isMap || Collection.class.isAssignableFrom(rawType) || rawType.isArray();
        MapKey mapKey = null;
        List<Annotation> loadValueIncompatibles = new ArrayList<Annotation>();
        // Note only qualifiers and bean loaders annotation are considered
        // effective annotation. Scope annotations is not effective here
        // because they are tagged on target type, not the field or method
        // parameter
        for (Annotation anno : aa) {
            Class<? extends Annotation> cls = anno.annotationType();
            if (Inject.class == cls || Provides.class == cls) {
                continue;
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
            if (cls.isAnnotationPresent(LoadValue.class)) {
                valueLoader = anno;
            } else if (cls.isAnnotationPresent(LoadCollection.class)) {
                if (isContainer) {
                    elementLoaders.add(anno);
                    loadValueIncompatibles.add(anno);
                } else {
                    Genie.logger.warn("LoadCollection annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                }
            } else if (Named.class == cls) {
                loadValueIncompatibles.add(anno);
                name = ((Named)anno).value();
            } else if (genie.isQualifier(cls)) {
                qualifiers.add(anno);
                loadValueIncompatibles.add(anno);
            } else if (cls.isAnnotationPresent(Filter.class)) {
                if (isContainer) {
                    filters.add(anno);
                    loadValueIncompatibles.add(anno);
                } else {
                    Genie.logger.warn("Filter annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                }
            } else if (genie.isPostConstructProcessor(cls)) {
                postProcessors.add(anno);
                annotations.put(cls, anno);
            } else {
                resolveScope(anno);
            }
        }
        if (isMap && hasElementLoader() && null == mapKey) {
            throw new InjectException("No MapKey annotation found on Map type target with ElementLoader annotation presented");
        }
        if (null != valueLoader) {
            if (!loadValueIncompatibles.isEmpty()) {
                throw new InjectException("ValueLoader annotation cannot be used with Qualifier, ElementLoader and Filter annotations: %s", annotations);
            }
            annotations.put(valueLoader.annotationType(), valueLoader);
        } else {
            for (Annotation anno: loadValueIncompatibles) {
                annotations.put(anno.annotationType(), anno);
            }
            if (null != mapKey) {
                if (hasElementLoader()) {
                    this.mapKey = mapKey;
                    annotations.put(mapKey.annotationType(), mapKey);
                } else {
                    Genie.logger.warn("MapKey annotation ignored on target without ElementLoader annotation presented");
                }
            }
        }
    }

    private void resolveScope(Annotation annotation) {
        Class<? extends Annotation> annoClass = annotation.annotationType();
        if (genie.isScope(annoClass)) {
            if (null != scope) {
                throw new InjectException("Multiple Scope annotation found: %s", this);
            }
            scope = annoClass;
        }
    }

    /**
     * Return hash code based on {@link #type} and {@link #annotations}.
     *
     * @return {@link Object#hashCode()} of this bean
     * @see #equals(Object)
     */
    private int calcHashCode() {
        return $.hc(type, name, annotations);
    }

    public static BeanSpec of(Class<?> clazz, Genie genie) {
        return new BeanSpec(clazz, null, genie);
    }

    public static BeanSpec of(Type type, Annotation[] paramAnnotations, Genie genie) {
        return new BeanSpec(type, paramAnnotations, genie);
    }

    static BeanSpec of(String name, Type type, Annotation[] paramAnnotations, Genie genie) {
        BeanSpec spec = of(type, paramAnnotations, genie);
        spec.name = name;
        return spec;
    }

}
