package org.osgl.genie;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicInteger;

class Circular {

    private static final AtomicInteger num = new AtomicInteger(0);

    protected int n;

    Circular() {
        n = num.incrementAndGet();
    }

    Circular(Circular circular) {
        this.n = circular.n;
    }

    static class A extends Circular {
        @Inject
        A(C c) {
            super(c);
        }
    }

    static class B extends Circular {
        @Inject
        B(A a) {
            super(a);
        }
    }

    static class C extends Circular {
        @Inject
        C(B b) {
            super(b);
        }
    }

    static class Self {
        @Inject
        Self self;
    }

}
