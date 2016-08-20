package org.osgl.inject;

import javax.inject.Inject;

public class UserService {
    @Inject Dao<User> dao;
}
