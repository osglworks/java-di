package org.osgl.inject;

import javax.inject.Inject;

public class OrderService {
    @Inject Dao<Order> dao;
}
