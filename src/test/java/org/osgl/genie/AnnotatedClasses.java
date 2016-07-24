package org.osgl.genie;

import org.osgl.genie.annotation.AnnotatedWith;
import org.osgl.genie.annotation.Provides;
import org.osgl.genie.loader.AnnotatedElementLoader;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.ws.BindingType;
import java.lang.annotation.Annotation;
import java.util.List;

class AnnotatedClasses {

    @Inject
    @AnnotatedWith(BindingType.class)
    private List<Object> bindingTypes;

    public List<Object> getBindingTypes() {
        return bindingTypes;
    }

    @Provides
    public static AnnotatedElementLoader annotatedElementLoaderProvider() {
        return new AnnotatedElementLoader() {
            @Override
            protected List<Class<?>> load(Class<? extends Annotation> annoClass, Genie genie) {
                if (annoClass == BindingType.class) {
                    return C.list(Foo.class, Bar.class);
                }
                throw E.unsupport();
            }
        };
    }

    @BindingType
    public static class Foo {
        @Override
        public String toString() {
            return "foo";
        }
    }

    @BindingType
    public static class Bar {
        @Override
        public String toString() {
            return "bar";
        }
    }

}
