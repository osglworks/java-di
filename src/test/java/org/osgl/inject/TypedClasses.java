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

import org.osgl.inject.annotation.Provides;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.inject.loader.TypedElementLoader;
import org.osgl.util.C;

import javax.inject.Inject;
import java.util.List;

public class TypedClasses {

    @Inject
    @TypeOf
    List<Base> publicImpls;

    @Inject
    @TypeOf(loadNonPublic = true)
    List<Base> withNonPublic;

    @Inject
    @TypeOf(loadAbstract = true, loadNonPublic = true, loadRoot = true)
    List<Class<Base>> allBaseTypes;

    public static class Base {
        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class Derived extends Base {}

    public static abstract class AbstractDerived extends Base {}

    static class NonPublicDerived extends Base {}

    @Provides
    public static TypedElementLoader typedElementLoader() {
        return new TypedElementLoader() {
            @Override
            protected List<Class> load(Class type, boolean loadNonPublic, boolean loadAbstract, boolean loadRoot) {
                if (type == Base.class) {
                    List<Class> list = (List) C.newList(Derived.class);
                    if (loadNonPublic) {
                        list.add(NonPublicDerived.class);
                    }
                    if (loadAbstract) {
                        list.add(AbstractDerived.class);
                    }
                    if (loadRoot) {
                        list.add(Base.class);
                    }
                    return list;
                }
                return C.list();
            }
        };
    }

}
