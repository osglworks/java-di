package issue;

import org.junit.Test;
import org.osgl.inject.Genie;
import org.osgl.inject.Module;
import org.osgl.inject.loader.TypedElementLoader;
import osgl.ut.TestBase;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GH56 extends TestBase  {

    public enum Color {R, G, B}

    @Inject
    public Map<String, Color> colorLookup;

    @Inject
    public List<Color> colorList;

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
        GH56 gh56 = genie.get(GH56.class);
        eq(3, gh56.colorList.size());
        eq(Color.B, gh56.colorLookup.get("B"));
    }
}
