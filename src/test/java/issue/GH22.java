package issue;

import org.junit.Test;
import org.osgl.$;
import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.inject.annotation.InjectTag;
import osgl.ut.TestBase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.validation.constraints.NotNull;

public class GH22 extends TestBase {

    private Genie genie = Genie.create();

    @Retention(RetentionPolicy.RUNTIME)
    @InjectTag
    public @interface MyInject {}

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface MyPreference {}

    private static class Foo {
        @Inject
        private String withInject;

        @MyInject
        private String withMyInject;

        @MyPreference
        private String withQualifier;

        @NotNull
        private String noDecorators;
    }

    @Test
    public void testInject() {
        has("withInject");
    }

    @Test
    public void testInjectTag() {
        has("withMyInject");
    }

    @Test
    public void testQualifier() {
        has("withQualifier");
    }

    @Test
    public void testNoDecorators() {
        no("noDecorators");
    }

    private BeanSpec spec(String fieldName) {
        return BeanSpec.of($.fieldOf(Foo.class, fieldName), genie);
    }

    private void has(String fieldName) {
        yes(spec(fieldName).hasInjectDecorator());
    }

    private void no(String fieldName) {
        no(spec(fieldName).hasInjectDecorator());
    }

}
