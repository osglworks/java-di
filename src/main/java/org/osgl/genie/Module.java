package org.osgl.genie;

import org.osgl.genie.Genie.Binder;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.List;
import java.util.Map;

public abstract class Module {

    private List<Binder> binders = C.newList();

    protected final <T> Binder<T> bind(Class<T> type) {
        Binder<T> binder = new Binder<T>(type);
        binders.add(binder);
        return binder;
    }

    protected abstract void configure();

    final void applyTo(Genie genie) {
        configure();
        validate();
        for (Binder<?> binder : binders) {
            binder.register(genie);
        }
    }

    private void validate() {
        Map<Object, Binder> map = C.newMap();
        for (Binder<?> binder : binders) {
            Object key = binder.key();
            if (map.containsKey(key)) {
                throw E.invalidConfiguration("Duplicate binder key found: ", key);
            }
            map.put(key, binder);
        }
    }

}
