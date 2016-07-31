package org.osgl.inject;

import org.osgl.inject.annotation.SessionScoped;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

@SessionScoped
class SessionPostConstruct {

    static final AtomicInteger instances = new AtomicInteger(0);

    @PostConstruct
    void init() {
        instances.incrementAndGet();
    }

}
