package issue;

import issue.gh42.Order;
import org.junit.Test;
import org.osgl.inject.Genie;
import osgl.ut.TestBase;

public class GH42 extends TestBase {
    @Test
    public void test() {
        Genie genie = Genie.create();
        Order.Dao orderDao = genie.get(Order.Dao.class);
        notNull(orderDao.accDao);
    }
}
