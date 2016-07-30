package org.osgl.inject;

import org.osgl.inject.Genie.Binder;
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
        validate(genie);
        for (Binder<?> binder : binders) {
            binder.register(genie);
        }
    }

    private void validate(Genie genie) {
        Map<Object, Binder> map = C.newMap();
        for (Binder<?> binder : binders) {
            Object spec = binder.beanSpec(genie);
            if (map.containsKey(spec)) {
                throw E.invalidConfiguration("Duplicate bean spec found: ", spec);
            }
            map.put(spec, binder);
        }
    }

}
