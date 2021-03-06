package issue;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2016 - 2018 OSGL (Open Source General Library)
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

import issue.gh42.Order;
import org.junit.Ignore;
import org.junit.Test;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

@Ignore
// no way to get this fixed in Genie
public class GH42 extends TestBase {
    @Test
    public void test() {
        Genie genie = Genie.create();
        Order.Dao orderDao = genie.get(Order.Dao.class);
        notNull(orderDao.accDao);
    }
}
