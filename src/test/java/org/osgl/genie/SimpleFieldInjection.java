package org.osgl.genie;

import javax.inject.Inject;

class SimpleFieldInjection {

    @Inject
    SimpleEmptyConstructor foo;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public SimpleEmptyConstructor foo() {
        return foo;
    }

}
