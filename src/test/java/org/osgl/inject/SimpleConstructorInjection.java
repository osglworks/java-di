package org.osgl.inject;

import javax.inject.Inject;

class SimpleConstructorInjection {

    SimpleEmptyConstructor foo;

    @Inject
    SimpleConstructorInjection(SimpleEmptyConstructor foo) {
        this.foo = foo;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public SimpleEmptyConstructor foo() {
        return foo;
    }
}
