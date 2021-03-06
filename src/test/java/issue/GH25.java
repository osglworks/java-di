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
import org.osgl.inject.Genie;
import org.osgl.inject.NamedProvider;
import osgl.ut.TestBase;

import javax.inject.Inject;
import javax.inject.Named;

public class GH25 extends TestBase {

    public static class Foo {
        private String name;
        public Foo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class FooProvider implements NamedProvider<Foo> {
        @Override
        public Foo get(String name) {
            return new Foo(name);
        }
    }

    public static class FooHolder {

        @Named("bar")
        @Inject
        private Foo foo;

    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        genie.registerNamedProvider(Foo.class, new FooProvider());
        FooHolder fooHolder = genie.get(FooHolder.class);
        eq("bar", fooHolder.foo.name);
    }

}
