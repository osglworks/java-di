package issue;

import org.junit.Test;
import org.osgl.$;
import org.osgl.Lang;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

import java.lang.reflect.Field;

public class Gh59 extends TestBase {
    @Test
    public void test() {
        Genie genie = Genie.create();
        eq(0, genie.get(Integer.class));
    }
}
