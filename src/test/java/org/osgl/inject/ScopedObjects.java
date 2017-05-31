package org.osgl.inject;

import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;
import org.osgl.inject.annotation.StopInheritedScope;
import org.osgl.util.S;

import javax.inject.Singleton;
import java.io.Serializable;
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

    @javax.enterprise.context.SessionScoped
    public static class JEESessionObject extends ScopedObjects implements SessionProduct, Serializable {}

    @SessionScoped
    public static class SessionObject extends ScopedObjects implements SessionProduct {}

    @InheritedStateless
    public static class StatelessBase extends ScopedObjects {}

    public static class StatelessBar extends StatelessBase {}

    @StopInheritedScope
    public static class StatefulZee extends StatelessBase {}

    @Stateful
    public static class StatefulFoo extends StatelessBase {}

    @SessionScoped
    public static class ConflictedScope extends StatelessBase {}

    @Singleton
    public static class CompatibleScope extends StatelessBase {}

    public static interface SingletonProduct {}

    public static interface RequestProduct {}

    public static interface SessionProduct {}

    public static class SingletonBean extends ScopedObjects implements SingletonProduct, SingletonBoundObject {}

    public static class SessionBean extends ScopedObjects implements SessionProduct {}

    public static interface SingletonBoundObject {}

}
