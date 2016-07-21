package org.osgl.genie;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.annotation.Filter;
import org.osgl.genie.annotation.Loader;
import org.osgl.genie.annotation.MapKey;
import org.osgl.genie.annotation.Provides;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Specification of a bean to be injected
 */
public class BeanSpec {

    /**
     * Used to sort annotation list so we can make equality compare between
     * `BeanSpec` generated from {@link Provides provider} factory method
     * and the `BeanSpec` generated from field or method parameters.
     * <p>
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
    private final Osgl.Var<MapKey> mapKey = $.var();
    private final Osgl.Var<Class<? extends Annotation>> scope = $.var();

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
    BeanSpec(Type type, Annotation[] annotations) {
        this.type = type;
        this.resolveType();
        this.resolveAnnotations(annotations);
        this.hc = $.hc(type, this.annotations);
    }

    private BeanSpec(BeanSpec providerSpec) {
        if (!providerSpec.isProvider()) {
            throw new IllegalStateException("not a provider spec");
        }
        this.type = ((ParameterizedType) providerSpec.type).getActualTypeArguments()[0];
        this.annotations.addAll(providerSpec.annotations);
        this.loaders.addAll(providerSpec.loaders);
        this.filters.addAll(providerSpec.filters);
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
        if (obj instanceof BeanSpec) {
            BeanSpec that = (BeanSpec) obj;
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

    BeanSpec rawTypeSpec() {
        return BeanSpec.of(rawType());
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

    BeanSpec providerSpec() {
        return new BeanSpec(this);
    }

    List<Annotation> annotations() {
        return annotations;
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

    BeanSpec scope(Class<? extends Annotation> scopeAnno) {
        scope.set(scopeAnno);
        return this;
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
        for (Annotation anno : aa) {
            Class cls = anno.annotationType();
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
            if (cls.isAnnotationPresent(Loader.class)) {
                if (isContainer) {
                    annotations.add(anno);
                    loaders.add(anno);
                } else {
                    Genie.logger.warn("Loader annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
                }
            } else if (cls.isAnnotationPresent(Qualifier.class)) {
                annotations.add(anno);
            } else if (cls.isAnnotationPresent(Filter.class)) {
                if (isContainer) {
                    annotations.add(anno);
                    filters.add(anno);
                } else {
                    Genie.logger.warn("Filter annotation[%s] ignored as target type is neither Collection nor Map", cls.getSimpleName());
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
                Genie.logger.warn("MapKey annotation ignored on target without ElementLoader annotation presented");
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

    static BeanSpec of(Class<?> clazz) {
        return new BeanSpec(clazz, null);
    }

    static BeanSpec of(Type type, Annotation[] paramAnnotations) {
        return new BeanSpec(type, paramAnnotations);
    }

}
