package issue;

import org.junit.Test;
import org.osgl.inject.Genie;
import org.osgl.inject.Module;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.inject.loader.TypedElementLoader;
import osgl.ut.TestBase;

import java.util.ArrayList;
import java.util.List;

public class GH55 extends TestBase {

    public enum Size {
        SMALL, MEDIUM, LARGE;
    }

    @TypeOf
    private List<Size> sizes;

    @Test
    public void test() {
        Genie genie = Genie.create(new Module() {
            @Override
            protected void configure() {
                bind(TypedElementLoader.class).to(new TypedElementLoader() {
                    @Override
                    protected List<Class> load(Class type, boolean loadNonPublic, boolean loadAbstract, boolean loadRoot) {
                        List<Class> list = new ArrayList<>();
                        list.add(Size.class);
                        return list;
                    }
                });
            }
        });
        GH55 gh55 = genie.get(GH55.class);
        eq(3, gh55.sizes.size());
    }

}
