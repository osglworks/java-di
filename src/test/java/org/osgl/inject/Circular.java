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

import javax.inject.Inject;
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
