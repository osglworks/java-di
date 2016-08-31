package org.osgl.inject;

/**
 * A `GenericTypedBeanLoader` can be used to load instance of certain type
 * with generic type parameters.
 *
 * A typical usage scenario is to load a `Dao` implementation. E.g.
 *
 * ```
 * public class FooService {
 *     {@literal @}Inject
 *      private Dao<Foo> fooDao;
 *      ...
 * }
 * ```
 *
 * One must register the `GenericTypedBeanLoader` via calling
 * the {@link Genie#registerGenericTypedBeanLoader(Class, GenericTypedBeanLoader)}
 * method
 */
public interface GenericTypedBeanLoader<T> {
    /**
     * Returns an instance matches the spec
     * @param spec the bean spec
     * @return the bean instance
     */
    T load(BeanSpec spec);
}
