package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.genie.BeanLoader;
import org.osgl.util.E;

import java.util.List;
import java.util.Map;

/**
 * Load beans whose class extends/implements specified class/interface (as hint)
 */
public abstract class TypedBeanLoader<T> extends BeanLoaderBase<T> implements BeanLoader<T> {

    /**
     * Loads a bean implementation which class implements/extends
     * the interface/class specified as `hint`.
     *
     * Note if there are multiple implementations matches `hint` then
     * the method will choose the first one it found to create the bean
     * instance
     *
     * @param hint the base class or interface class
     * @param options not used in this method
     * @return a bean instance whose class is a sub class or implementation of `hint`
     */
    @Override
    public T loadOne(Object hint, Map<String, Object> options) {
        E.illegalArgumentIf(!(hint instanceof Class));
        return (T) loadOne((Class) hint);
    }

    /**
     * This method will load instances of all public and non-abstract classes that
     * implements/extends the interface/class specified as `hint`
     *
     * @param hint the base class or interface class
     * @param options not used in this method
     * @return the list of bean instances whose class is sub class or implementation of `hint`
     */
    @Override
    public List<T> loadMultiple(Object hint, Map<String, Object>  options) {
        E.illegalArgumentIf(!(hint instanceof Class));
        return (List<T>) loadMultiple((Class) hint);
    }

    /**
     * This method returns a predicate function that test the bean instance against the
     * class specified by `hint`. If the bean is an instance of the `hint` class, then
     * the predicate function returns `true` otherwise it returns `false`
     *
     * @param hint the base class or interface class
     * @param options Not used in this method
     * @return a predicate function whose behavior is described above
     */
    @Override
    public Osgl.Function<T, Boolean> filter(Object hint, Map<String, Object>  options) {
        E.illegalArgumentIf(!(hint instanceof Class));
        final Class baseClass = (Class) hint;
        return new $.Predicate<T>() {
            @Override
            public boolean test(T o) {
                return baseClass.isAssignableFrom(o.getClass());
            }
        };
    }

    /**
     * Load a bean that is type or implementation of specified `type`
     * @param type the class or interface specification
     * @return a bean as described above
     */
    protected abstract T loadOne(Class<T> type);

    /**
     * Load a list of beans whose class is type or implementation of the
     * specified `type`
     * @param type the class or interface specification
     * @return a list of beans as described
     */
    protected abstract List<T> loadMultiple(Class<T> type);

}
