package issue;

import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.util.Generics;
import osgl.ut.TestBase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.inject.Named;

public class GH43 extends TestBase {

    public static abstract class Foo<ID_TYPE> {
        public void doIt(@Named("foo") List<ID_TYPE> id) {}
    }

    public static class IntFoo extends Foo<Integer> {
    }

    @Test
    public void test() {
        Genie genie = Genie.create();
        Method method = $.getMethod(Foo.class, "doIt", List.class);
        Map<String, Class> typeVarLookup = Generics.buildTypeParamImplLookup(IntFoo.class);
        Type type = method.getGenericParameterTypes()[0];
        Annotation[] anno = method.getParameterAnnotations()[0];
        BeanSpec spec = BeanSpec.of(type, anno, genie, typeVarLookup);
    }

}
