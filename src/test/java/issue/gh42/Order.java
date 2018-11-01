package issue.gh42;

import javax.inject.Inject;

public class Order extends BsbfRecord<Order> {
    public static class Dao extends BsbfDao<Order> {
        @Inject
        public MorphiaDao<Account> accDao;
    }
}
