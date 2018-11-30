package issue;

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
