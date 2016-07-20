package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.util.E;

import java.util.List;
import java.util.Map;

/**
 * Load beans whose class extends/implements specified class/interface (as hint)
 */
public abstract class TypedBeanLoader<T> extends ElementLoaderBase<T> {

    /**
     * This method will load instances of all public and non-abstract classes that
     * implements/extends the interface/class specified as `value` option
     *
     * @param options must contains an entry indexed with "value" and the value should be a Class type
     * @return the list of bean instances whose class is sub class or implementation of `hint`
     */
    @Override
    public Iterable<T> load(Map<String, Object>  options) {
        Object hint = options.get("value");
        E.illegalArgumentIf(!(hint instanceof Class));
        return load((Class<T>) hint);
    }

    /**
     * This method returns a predicate function that test the bean instance against the
     * class specified by `hint`. If the bean is an instance of the `hint` class, then
     * the predicate function returns `true` otherwise it returns `false`
     *
     * @param options must contains an entry indexed with "value" and the value should be a Class type
     * @return a predicate function whose behavior is described above
     */
    @Override
    public Osgl.Function<T, Boolean> filter(Map<String, Object>  options) {
        Object hint = options.get("value");
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
     * Load a list of beans whose class is type or implementation of the
     * specified `type`
     * @param type the class or interface specification
     * @return a list of beans as described
     */
    protected abstract List<T> load(Class<T> type);

}
