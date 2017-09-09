package issue;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2017 OSGL (Open Source General Library)
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
import org.osgl.inject.Genie;
import org.osgl.inject.Module;
import org.osgl.ut.TestBase;

/**
 * [Genie shall use default constructor when possible]
 * (https://github.com/osglworks/java-di/issues/10)
 */
public class GH10 extends TestBase {

    public interface A {
        String a();
    }

    public static class Foo implements A {

        private String a;

        public Foo() {
            a = "foo";
        }

        public Foo(String a) {
            this.a = a;
        }

        @Override
        public String a() {
            return a;
        }
    }

    public static class Binder extends Module {
        @Override
        protected void configure() {
            bind(A.class).to(Foo.class);
        }
    }

    @Test
    public void test() {
        Genie genie = Genie.create(Binder.class);
        A a = genie.get(A.class);
        eq("foo", a.a());
    }

}
