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
import org.osgl.inject.Module;
import org.osgl.inject.loader.TypedElementLoader;
import osgl.ut.TestBase;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GH57 extends TestBase  {

    public static class TestResp<T> {
        public Integer code;
        public T result;

        public TestResp(Integer code, T result) {
            this.code = code;
            this.result = result;
        }
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(TestResp.class, genie);
        Map<String, BeanSpec> fieldSpecs = spec.fields();
        eq(Object.class, fieldSpecs.get("result").rawType());
        eq(Integer.class, fieldSpecs.get("code").rawType());
    }
}
