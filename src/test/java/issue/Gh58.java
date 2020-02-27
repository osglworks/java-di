package issue;

import org.junit.Test;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

public class Gh58 extends TestBase {

    public static class Foo {
        public int id;
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        Integer i = genie.get(Integer.class);
        BeanSpec fooSpec = BeanSpec.of(Foo.class, genie);
        no(genie.subjectToInject(fooSpec));
        genie.get(Foo.class);
        no(genie.subjectToInject(fooSpec));
    }

}
