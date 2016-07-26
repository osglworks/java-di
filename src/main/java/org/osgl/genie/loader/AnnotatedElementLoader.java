package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.BeanSpec;
import org.osgl.genie.ElementType;
import org.osgl.genie.Genie;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Implementation of `AnnotatedElementLoader` shall load beans whose class has
 * been annotated by a certain annotation class
 */
public abstract class AnnotatedElementLoader extends ElementLoaderBase<Object> {


    /**
     * This method will load instances of all public and non-abstract classes that
     * has been annotated by annotation class specified as `value` in `options`
     *
     * @param options   optional parameters specified to refine the loading process
     * @param container the bean spec about the container into which the bean to be loaded
     * @param genie     the dependency injector
     * @return the list of bean instances
     */
    @Override
    public Iterable<Object> load(Map<String, Object> options, BeanSpec container, final Genie genie) {
        Object hint = options.get("value");
        E.illegalArgumentIf(!Annotation.class.isAssignableFrom((Class) hint));
        boolean loadNonPublic = (Boolean)options.get("loadNonPublic");
        ElementType elementType = (ElementType)options.get("elementType");
        boolean loadAbstract = elementType.loadAbstract() && (Boolean) options.get("loadAbstract");
        List<Class<?>> classes = load(annoClassFromHint(hint), loadNonPublic, loadAbstract);
        return elementType.transform(classes, genie);
    }

    /**
     * Implementation shall load list of beans whose class is annotated with `annoClass`
     * specified. The class of all beans returned must have `public` access
     *
     * @param annoClass the annotation class
     * @param loadNonPublic specify if it should load non public classes
     * @param loadAbstract specify if it should load abstract classes
     * @return a list of classes that are annotated with `annoClass`
     */
    protected abstract List<Class<?>> load(
            Class<? extends Annotation> annoClass,
            boolean loadNonPublic,
            boolean loadAbstract);

    /**
     * Returns a predicate check if an object has annotation as specified as `hint`
     *
     * @param options   not used
     * @param container the bean spec of the container into which the element will be loaded
     * @return a predicate function as described above
     */
    @Override
    public Osgl.Function filter(Map<String, Object> options, BeanSpec container) {
        Object hint = options.get("value");
        E.illegalArgumentIf(!Annotation.class.isAssignableFrom((Class) hint));
        final Class<? extends Annotation> annoClass = annoClassFromHint(hint);
        final ElementType elementType = (ElementType) options.get("elementType");
        final boolean loadNonPublic = (Boolean)options.get("loadNonPublic");
        final boolean loadAbstract = elementType.loadAbstract() && (Boolean) options.get("loadAbstract");
        return new Osgl.Predicate() {
            @Override
            public boolean test(Object o) {
                if (elementType == ElementType.BEAN) {
                    Class<?> c = o.getClass();
                    return (loadNonPublic || Modifier.isPublic(c.getModifiers())) && c.isAnnotationPresent(annoClass);
                } else {
                    if (o instanceof Class) {
                        Class c = (Class) o;
                        int modifiers = c.getModifiers();
                        boolean yes = loadNonPublic || Modifier.isPublic(modifiers);
                        yes = yes && loadAbstract || !Modifier.isAbstract(modifiers);
                        yes = yes && c.isAnnotationPresent(annoClass);
                        return yes;
                    }
                    return false;
                }
            }
        };
    }

    private static Class<? extends Annotation> annoClassFromHint(Object hint) {
        return $.cast(hint);
    }


}
