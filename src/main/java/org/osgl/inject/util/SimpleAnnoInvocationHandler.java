package org.osgl.inject.util;

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
import org.osgl.util.S;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * The class is designed to create {@link java.lang.annotation.Annotation}
 * proxy instance for given annotation type and without member values
 */
class SimpleAnnoInvocationHandler implements InvocationHandler {
    private final Class<? extends Annotation> type;

    SimpleAnnoInvocationHandler(Class<? extends Annotation> type) {
        this.type = type;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        if (S.eq("hashCode", methodName)) {
            return hashCode();
        } else if (S.eq("equals", methodName)) {
            return equals(args[0]);
        } else if (S.eq("annotationType", methodName)) {
            return type;
        } else if (S.eq("toString", methodName)) {
            return toString();
        }

        Object result = method.getDefaultValue();

        if (result == null) {
            throw new IncompleteAnnotationException(type, methodName);
        }

        return result;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Method m : type.getDeclaredMethods()) {
            Object o = m.getDefaultValue();
            if (null == o) {
                throw new IncompleteAnnotationException(type, m.getName());
            }
            result += AnnotationUtil.hashMember(m.getName(), o);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!type.isInstance(obj)) {
            return false;
        }

        if (isUs(obj)) {
            return true;
        }

        for (Method m : type.getDeclaredMethods()) {
            Object thisVal = m.getDefaultValue();
            Object thatVal;
            try {
                thatVal = m.invoke(obj);
            } catch (InvocationTargetException e) {
                return false;
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
            if (!$.eq2(thatVal, thisVal)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private static boolean isUs(Object o) {
        if (Proxy.isProxyClass(o.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(o);
            return handler instanceof SimpleAnnoInvocationHandler;
        }
        return false;
    }
}
