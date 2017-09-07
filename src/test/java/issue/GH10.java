package issue;

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
