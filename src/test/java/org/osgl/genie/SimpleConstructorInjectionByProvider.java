package org.osgl.genie;

import javax.inject.Inject;
import javax.inject.Provider;

class SimpleConstructorInjectionByProvider {

    private SimpleEmptyConstructor foo;

    @Inject
    public void initFoo(Provider<SimpleEmptyConstructor> foo) {
        this.foo = foo.get();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public SimpleEmptyConstructor foo() {
        return foo;
    }

}
