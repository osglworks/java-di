package org.osgl.genie;

import org.osgl.genie.annotation.RequestScoped;
import org.osgl.genie.annotation.SessionScoped;
import org.osgl.util.S;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ScopedObjects {

    private static final AtomicInteger NG = new AtomicInteger(0);

    private int id;

    private ScopedObjects() {
        id = NG.incrementAndGet();
    }

    public int id() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return S.fmt("%s[%s]", getClass().getSimpleName(), id);
    }

    @Singleton
    public static class SingletonObject extends ScopedObjects implements SingletonProduct {
    }

    @RequestScoped
    public static class RequestObject extends ScopedObjects implements RequestProduct {}

    @SessionScoped
    public static class SessionObject extends ScopedObjects implements SessionProduct {}

    public static interface SingletonProduct {}

    public static interface RequestProduct {}

    public static interface SessionProduct {}

    public static class SingletonBean extends ScopedObjects implements SingletonProduct, SingletonBoundObject {}

    public static class SessionBean extends ScopedObjects implements SessionProduct {}

    public static interface SingletonBoundObject {}

}
