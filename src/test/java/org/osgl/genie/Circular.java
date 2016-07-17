package org.osgl.genie;

import javax.inject.Inject;

class Circular {

    static class A {
        @Inject
        A(C c) {}
    }

    static class B {
        @Inject
        B(A a) {}
    }

    static class C {
        @Inject
        C(B b) {}
    }

    static class Self {
        @Inject
        Self self;
    }

}
