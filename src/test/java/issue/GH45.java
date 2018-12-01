package issue;

import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.util.C;
import osgl.ut.TestBase;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GH45 extends TestBase {

    class Foo<ID> {
        List<ID> list;
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        Field field = $.fieldOf(Foo.class, "list");
        Type type = field.getGenericType();
        Map<String, Class> typeLookup = C.Map("ID", Long.class);
        BeanSpec spec = BeanSpec.of(type, genie, typeLookup);
        List<Type> typeParams = spec.typeParams();
        eq(Long.class, typeParams.get(0));
    }

}
