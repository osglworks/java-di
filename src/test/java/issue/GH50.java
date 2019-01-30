package issue;

import org.junit.Test;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

import javax.inject.Inject;

public class GH50 extends TestBase {

    public static class Foo {}

    public static class GrandParent<T> {
        @Inject
        T data;
    }

    public static class Parent<T> extends GrandParent<T> {}

    public static class Me extends Parent<Foo> {}

    @Test
    public void test() {
        Genie genie = Genie.create();
        genie.get(Me.class);
    }

}
