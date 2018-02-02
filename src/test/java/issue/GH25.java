package issue;

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
