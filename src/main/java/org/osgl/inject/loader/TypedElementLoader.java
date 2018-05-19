package org.osgl.inject.loader;

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
import org.osgl.inject.BeanSpec;
import org.osgl.inject.ElementType;
import org.osgl.inject.Genie;
import org.osgl.inject.InjectException;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.util.E;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Load beans whose class extends/implements specified class/interface (as hint)
 */
public abstract class TypedElementLoader<T> extends ElementLoaderBase<T> {

    /**
     * This method will load instances of all public and non-abstract classes that
     * implements/extends the interface/class specified as `value` option
     *
     * @param options   must contains an entry indexed with "value" and the value should be a Class type
     * @param container the bean spec of the container into which the element will be loaded
     * @param genie     the dependency injector
     * @return the list of bean instances whose class is sub class or implementation of `hint`
     */
    @Override
    public final Iterable<T> load(Map<String, Object> options, BeanSpec container, final Genie genie) {
        ElementType elementType = (ElementType)options.get("elementType");
        boolean loadNonPublic = (Boolean)options.get("loadNonPublic");
        boolean loadRoot = (Boolean) options.get("loadRoot");
        $.Var<ElementType> typeVar = $.var(elementType);
        Class<T> targetClass = targetClass(typeVar, options, container);
        boolean loadAbstract = typeVar.get().loadAbstract() && (Boolean) options.get("loadAbstract");
        List<Class<? extends T>> classes = load(targetClass, loadNonPublic, loadAbstract, loadRoot);
        return (Iterable<T>)typeVar.get().transform((List)classes, genie);
    }

    /**
     * This method returns a predicate function that test the bean instance against the
     * class specified by `hint`. If the bean is an instance of the `hint` class, then
     * the predicate function returns `true` otherwise it returns `false`
     *
     * @param options   must contains an entry indexed with "value" and the value should be a Class type
     * @param container the bean spec of the container into which the element will be loaded
     * @return a predicate function whose behavior is described above
     */
    @Override
    public final $.Function<T, Boolean> filter(Map<String, Object> options, BeanSpec container) {
        $.Var<ElementType> typeVar = $.var((ElementType) options.get("elementType"));
        final Class baseClass = targetClass(typeVar, options, container);
        final ElementType elementType = typeVar.get();
        final boolean loadNonPublic = (Boolean)options.get("loadNonPublic");
        final boolean loadAbstract = elementType.loadAbstract() && (Boolean) options.get("loadAbstract");
        final boolean loadRoot = (Boolean) options.get("loadRoot");
        return new $.Predicate<T>() {
            @Override
            public boolean test(T o) {
                if (elementType == ElementType.BEAN) {
                    Class<?> c = o.getClass();
                    return (loadNonPublic || Modifier.isPublic(c.getModifiers()))
                            && (baseClass.isAssignableFrom(c)
                            && (loadRoot || baseClass != c)
                    );
                } else {
                    if (o instanceof Class) {
                        Class c = (Class) o;
                        int modifiers = c.getModifiers();
                        boolean yes = loadNonPublic || Modifier.isPublic(modifiers);
                        yes = yes && (loadAbstract || !Modifier.isAbstract(modifiers));
                        yes = yes && baseClass.isAssignableFrom(c);
                        yes = yes && (loadRoot || baseClass != c);
                        return yes;
                    }
                    return false;
                }
            }
        };
    }


    /**
     * Load a list of beans whose class is type or implementation of the
     * specified `type`
     *
     * @param type the class or interface specification
     * @param loadNonPublic specify if it should load non public classes
     * @param loadAbstract specify if it should load abstract classes
     * @return a list of beans as described
     */
    protected abstract List<Class<? extends T>> load(
            Class<T> type,
            boolean loadNonPublic,
            boolean loadAbstract,
            boolean loadRoot);

    private Class<T> targetClass($.Var<ElementType> typeVar, Map<String, Object> options, BeanSpec container) {
        Object hint = options.get("value");
        E.illegalArgumentIf(!(hint instanceof Class));
        Class<?> targetClass = $.cast(hint);
        Class<?> inferredTargetClass = LoaderUtil.targetClass(typeVar, container);
        if (null != inferredTargetClass) {
            if (TypeOf.PlaceHolder.class == targetClass) {
                targetClass = inferredTargetClass;
            } else if (!inferredTargetClass.isAssignableFrom(targetClass)) {
                throw new InjectException("specified class[%s] doesn't match the container spec: %s", targetClass, container);
            }
        } else if (TypeOf.PlaceHolder.class == targetClass) {
            if (TypeOf.PlaceHolder.class == targetClass) {
                throw new InjectException("Cannot load element - target type info is missing");
            }
        }
        return $.cast(targetClass);
    }

}
