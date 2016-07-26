package org.osgl.genie.loader;

import org.osgl.$;
import org.osgl.genie.BeanSpec;
import org.osgl.genie.ElementType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

class LoaderUtil {
    static Class<?> targetClass($.Var<ElementType> typeVar, BeanSpec container) {
        List<Type> types = container.typeParams();
        Class targetClass = null;
        if (!types.isEmpty()) {
            // the effective type is always the last one
            // this is for both Collection and Map
            Type type = types.get(types.size() - 1);
            if (type instanceof Class) {
                targetClass = $.cast(type);
                if (targetClass == Class.class) {
                    typeVar.set(ElementType.CLASS);
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType ptype = $.cast(type);
                if (ptype.getRawType() instanceof Class) {
                    type = ptype.getActualTypeArguments()[0];
                    if (type instanceof Class) {
                        targetClass = $.cast(type);
                        typeVar.set(ElementType.CLASS);
                    }
                }
            }
        }
        return targetClass;
    }
}
