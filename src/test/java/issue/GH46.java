package issue;

import org.junit.Test;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

public class GH46 extends TestBase {

    @Test
    public void test() {
        Genie genie = Genie.create();
        BeanSpec spec = BeanSpec.of(char.class, genie);
        isNull(spec.parent());
        isEmpty(spec.fields());
    }

}
