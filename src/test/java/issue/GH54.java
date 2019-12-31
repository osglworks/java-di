package issue;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2019 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
