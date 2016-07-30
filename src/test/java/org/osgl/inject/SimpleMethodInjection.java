package org.osgl.inject;

import javax.inject.Inject;

class SimpleMethodInjection {

    private SimpleEmptyConstructor foo;

    @Inject
    public void initFoo(SimpleEmptyConstructor foo) {
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
