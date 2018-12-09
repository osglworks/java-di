package issue;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2018 OSGL (Open Source General Library)
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
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.util.C;
import osgl.ut.TestBase;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GH45 extends TestBase {

    class Foo<ID> {
        List<ID> list;
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        Field field = $.fieldOf(Foo.class, "list");
        Type type = field.getGenericType();
        Map<String, Class> typeLookup = C.Map("ID", Long.class);
        BeanSpec spec = BeanSpec.of(type, genie, typeLookup);
        List<Type> typeParams = spec.typeParams();
        eq(Long.class, typeParams.get(0));
    }

}
