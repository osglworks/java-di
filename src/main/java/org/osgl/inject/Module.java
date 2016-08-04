package org.osgl.inject;

import org.osgl.inject.Genie.Binder;
import org.osgl.util.C;
import org.osgl.util.E;

import java.lang.annotation.Annotation;
import java.util.*;

public abstract class Module {

    private List<Binder> binders = new ArrayList<Binder>();
    private Set<Class<? extends Annotation>> qualifiers = new HashSet<Class<? extends Annotation>>();

    protected final <T> Binder<T> bind(Class<T> type) {
        Binder<T> binder = new Binder<T>(type);
        binders.add(binder);
        return binder;
    }

    protected final Module registerQualifiers(Class<? extends Annotation> ... qualifiers) {
        this.qualifiers.addAll(C.listOf(qualifiers));
        return this;
    }

    protected abstract void configure();

    final void applyTo(Genie genie) {
        configure();
        validate(genie);
        for (Binder<?> binder : binders) {
            binder.register(genie);
        }
        genie.registerQualifiers(qualifiers);
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
