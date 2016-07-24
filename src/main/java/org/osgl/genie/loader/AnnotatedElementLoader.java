package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.BeanSpec;
import org.osgl.genie.Genie;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
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
        List<Class<?>> classes = load(annoClassFromHint(hint), genie);
        return C.list(classes).map(new $.Transformer<Class, Object>() {
            @Override
            public Object transform(Class aClass) {
                return genie.get(aClass);
            }
        });
    }

    /**
     * Implementation shall load list of beans whose class is annotated with `annoClass`
     * specified. The class of all beans returned must have `public` access
     *
     * @param annoClass the annotation class
     * @param genie     dependency injector used to load element instances
     * @return a list of classes that are annotated with `annoClass`
     */
    protected abstract List<Class<?>> load(Class<? extends Annotation> annoClass, Genie genie);

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
        return new Osgl.Predicate() {
            @Override
            public boolean test(Object o) {
                return o.getClass().getAnnotation(annoClass) != null;
            }
        };
    }

    private static Class<? extends Annotation> annoClassFromHint(Object hint) {
        return $.cast(hint);
    }


}
