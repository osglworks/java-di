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
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.util.KVStore;
import osgl.ut.TestBase;

import java.util.List;

public class GH47 extends TestBase {

    public static class Foo {
        public KVStore kv;
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(Foo.class, genie);
        List<BeanSpec> fields = spec.nonStaticFields();
        spec = fields.get(0);
        fields = spec.nonStaticFields();
        eq(9, fields.size());
    }

}
