package issue;

import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.inject.annotation.Configuration;
import osgl.ut.TestBase;

import java.lang.reflect.Field;
import javax.enterprise.context.SessionScoped;

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
