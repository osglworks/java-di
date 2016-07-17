package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.BeanLoader;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Implementation of `AnnotatedBeanLoader` shall load beans whose class has
 * been annotated by a certain annotation class
 */
public abstract class AnnotatedBeanLoader extends BeanLoaderBase<Object> implements BeanLoader<Object> {

    /**
     * This method will load a bean implementation which class has been
     * annotated with annotation class equals to `hint` specified.
     *
     * Note if there are multiple implementations annotated by `hint` then
     * the method will choose the first one it found to create the bean
     * instance
     *
     * @param hint the annotation class
     * @param options not used in this method
     * @return a bean instance which class has been annotated by `hint`
     */
    @Override
    public Object loadOne(Object hint, Map<String, Object> options) {
        E.illegalArgumentIf(Annotation.class.isAssignableFrom((Class) hint));
        return loadOne(annoClassFromHint(hint));
    }

    /**
     * This method will load instances of all public and non-abstract classes that
     * has been annotated by annotation class specified as `hint`
     *
     * @param hint the hint to specify the bean instances to be loaded
     * @param options optional parameters specified to refine the loading process
     * @return the list of bean instances
     */
    @Override
    public List<Object> loadMultiple(Object hint, Map<String, Object> options) {
        E.illegalArgumentIf(Annotation.class.isAssignableFrom((Class) hint));
        return loadMultiple(annoClassFromHint(hint));
    }

    /**
     * Implementation shall load a bean whose class is annotated with `annoClass`
     * specified. The class must have the `public` access
     * @param annoClass the annotation class
     * @return the bean loaded as described above
     */
    protected abstract Object loadOne(Class<? extends Annotation> annoClass);

    /**
     * Implementation shall load list of beans whose class is annotated with `annoClass`
     * specified. The class of all beans returned must have `public` access
     *
     * @param annoClass the annotation class
     * @return a list of beans as described above
     */
    protected abstract List<Object> loadMultiple(Class<? extends Annotation> annoClass);

    /**
     * Returns a predicate check if an object has annotation as specified as `hint`
     * @param hint the annotation class
     * @param options not used
     * @return a predicate function as described above
     */
    @Override
    public Osgl.Function filter(final Object hint, Map<String, Object> options) {
        E.illegalArgumentIf(Annotation.class.isAssignableFrom((Class) hint));
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
