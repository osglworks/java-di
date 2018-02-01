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

import org.junit.Ignore;
import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.inject.annotation.Configuration;
import osgl.ut.TestBase;

import java.lang.reflect.Field;
import javax.enterprise.context.SessionScoped;

// Scope annotation is a decorator for inject library
// to decide the inject logic. It is not a spec of
// the inject element
@Ignore
public class GH21 extends TestBase {
    private static class X {
        @Configuration("foo")
        public String foo;
    }

    private static class Y {
        @Configuration("foo")
        @SessionScoped
        public String foo;
    }

    @Test
    public void testEquality() {
        Genie genie = Genie.create();
        Field xfoo = $.fieldOf(X.class, "foo");
        Field yfoo = $.fieldOf(Y.class, "foo");
        assertNotEquals(BeanSpec.of(xfoo, genie), BeanSpec.of(yfoo, genie));
    }
}
