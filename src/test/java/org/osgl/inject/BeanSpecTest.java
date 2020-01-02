package org.osgl.inject;

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

import org.junit.Test;
import org.osgl.inject.annotation.InjectTag;
import osgl.ut.TestBase;

import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.inject.Qualifier;

public class BeanSpecTest extends TestBase {

    @Test
    public void testTaggedAnnotation() throws Exception {
        Constructor<LeatherSmoother.Host> constructor = LeatherSmoother.Host.class.getConstructor(LeatherSmoother.class);
        Annotation[] annotations = constructor.getParameterAnnotations()[0];
        Type paramType = constructor.getGenericParameterTypes()[0];
        BeanSpec spec = BeanSpec.of(paramType, annotations, Genie.create());
        eq(1, spec.taggedAnnotations(Qualifier.class).length);
        eq(0, spec.taggedAnnotations(InjectTag.class).length);
        eq(0, spec.taggedAnnotations(Retention.class).length);
        eq(0, spec.taggedAnnotations(Target.class).length);
        eq(0, spec.taggedAnnotations(Inherited.class).length);
        eq(0, spec.taggedAnnotations(Documented.class).length);
    }

    public static class Foo {
        public void getVoid() {}

        public String getString() {
            return "";
        }

        public String getStringWithArgs(int i) {
            return "";
        }

        public String setGetString() {
            return "";
        }

        private String getPrivate() {
            return "";
        }

        public static String getStatic() {
            return "";
        }

        private void setPrivate(String s) {}

        public void setString(String s) {}

        public String setNonVoidReturn(String s) {return s;}

        public static void setStatic(String s) {}

        public void setNoArgs() {}

        public void setTwoArgs(String s, int i) {}
    }

    @Test
    public void testIsGetter() throws Exception {
        yes(isGetter("getString"));
        no(isGetter("getVoid"));
        no(isGetter("getStringWithArgs"));
        no(isGetter("setGetString"));
        no(isGetter("getPrivate"));
        no(isGetter("getStatic"));
    }

    @Test
    public void testIsSetter() throws Exception {
        yes(isSetter("setString"));
        no(isSetter("setNonVoidReturn"));
        no(isSetter("setPrivate"));
        no(isSetter("setStatic"));
        no(isSetter("setNoArgs"));
        no(isSetter("setTwoArgs"));
    }

    private boolean isSetter(String methodName) {
        Method method = getMethod(methodName);
        return BeanSpec.isSetter(method);
    }

    private boolean isGetter(String methodName) {
        Method method = getMethod(methodName);
        return BeanSpec.isGetter(method);
    }

    private Method getMethod(String methodName) {
        for (Method method : Foo.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
