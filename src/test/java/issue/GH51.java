package issue;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2019 OSGL (Open Source General Library)
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
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

public class GH51 extends TestBase {

    public static class GrandParent<T> {
        T t;
    }

    public static class Req<V, ID> {
        ID id;
        V v;
    }

    public static class Parent<K, V, RQ extends Req<K, V>> extends GrandParent<RQ> {
    }

    public static class Me extends Parent<String, Integer, Req<String, Integer>> {}

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(Me.class, genie);
        BeanSpec t = spec.field("t");
        BeanSpec id = t.field("id");
        eq(Integer.class, id.rawType());
    }
}
