package issue;

import org.junit.Test;
import org.osgl.inject.Genie;
import org.osgl.inject.Module;
import org.osgl.inject.annotation.MapKey;
import org.osgl.inject.annotation.TypeOf;
import org.osgl.inject.loader.TypedElementLoader;
import org.osgl.util.Keyword;
import osgl.ut.TestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GH54 extends TestBase {
    public enum Color {
        LightGray, DarkBlue
    }

    @TypeOf
    @MapKey("name")
    private Map<Keyword, Color> colors;

    @Test
    public void test() {
        Genie genie = Genie.create(new Module() {
            @Override
            protected void configure() {
                bind(TypedElementLoader.class).to(new TypedElementLoader() {
                    @Override
                    protected List<Class> load(Class type, boolean loadNonPublic, boolean loadAbstract, boolean loadRoot) {
                        List<Class> list = new ArrayList<>();
                        list.add(Color.class);
                        return list;
                    }
                });
            }
        });
        GH54 gh54 = genie.get(GH54.class);
        eq(Color.DarkBlue, gh54.colors.get(Keyword.of("dark-blue")));
    }

}
