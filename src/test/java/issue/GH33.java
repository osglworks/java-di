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
import osgl.ut.TestBase;

import java.lang.reflect.Type;
import java.util.List;

public class GH33 extends TestBase {

    private static class PackItem {
        public String name;
    }

    public static class BaseMapper<T> {
    }

    public static class ItemMapper extends BaseMapper<PackItem> {
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(ItemMapper.class, genie);
        BeanSpec parent = spec.parent();
        List<Type> typeParams = parent.typeParams();
        eq(1, typeParams.size());
        eq(PackItem.class, typeParams.get(0));
    }

}
