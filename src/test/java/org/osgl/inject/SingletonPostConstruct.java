package org.osgl.inject;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
class SingletonPostConstruct {

    static final AtomicInteger instances = new AtomicInteger(0);

    @PostConstruct
    void init() {
        instances.incrementAndGet();
    }

}
