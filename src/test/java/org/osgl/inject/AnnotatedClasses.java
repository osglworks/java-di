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

import org.osgl.inject.annotation.AnnotatedWith;
import org.osgl.inject.annotation.Provides;
import org.osgl.inject.loader.AnnotatedElementLoader;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.inject.Inject;
import javax.xml.ws.BindingType;
import java.lang.annotation.Annotation;
import java.util.List;

class AnnotatedClasses {

    @Inject
    @AnnotatedWith(BindingType.class)
    private List<Object> publicBeans;

    @Inject
    @AnnotatedWith(value = BindingType.class, loadNonPublic = true)
    private List<Object> withPrivateBeans;

    @Inject
    @AnnotatedWith(
            value = BindingType.class,
            loadNonPublic = true,
            loadAbstract = true
    )
    private List<Class> withPrivateAndAbstractClasses;

    public List<Object> getPublicBeans() {
        return publicBeans;
    }

    public List<Object> getWithPrivateBeans() {
        return withPrivateBeans;
    }

    public List<Class> getWithPrivateAndAbstractClasses() {
        return withPrivateAndAbstractClasses;
    }

    @Provides
    public static AnnotatedElementLoader annotatedElementLoaderProvider() {
        return new AnnotatedElementLoader() {
            @Override
            protected List<Class<?>> load(
                    Class<? extends Annotation> annoClass,
                    boolean loadNonPublic,
                    boolean loadAbstract
            ) {
                if (annoClass == BindingType.class) {
                    if (loadNonPublic && loadAbstract) {
                        return C.list(PublicAnnotated.class, PrivateAnnotated.class, AbstractAnnotated.class);
                    }
                    if (loadNonPublic) {
                        return C.list(PublicAnnotated.class, PrivateAnnotated.class);
                    }
                    if (loadAbstract) {
                        return C.list(PublicAnnotated.class, AbstractAnnotated.class);
                    }
                    return C.<Class<?>>list(PublicAnnotated.class);
                }
                throw E.unsupport();
            }
        };
    }

    @BindingType
    public static class PublicAnnotated {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    @BindingType
    private static class PrivateAnnotated {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    @BindingType
    public abstract static class AbstractAnnotated {
        public String toString() {
            throw E.unsupport();
        }
    }

}
