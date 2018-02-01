package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
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

import org.osgl.inject.annotation.RequestScoped;
import org.osgl.inject.annotation.SessionScoped;
import org.osgl.inject.annotation.StopInheritedScope;
import org.osgl.util.S;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;

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

    public interface SingletonProduct {}

    public interface RequestProduct {}

    public interface SessionProduct {}

    public static class SingletonBean extends ScopedObjects implements SingletonProduct, SingletonBoundObject {}

    public static class SessionBean extends ScopedObjects implements SessionProduct {}

    public static interface SingletonBoundObject {}

}
