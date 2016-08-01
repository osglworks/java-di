package org.osgl.inject;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

class BaseWithPostConstructor {

    static ThreadLocal<BaseWithPostConstructor> current = new ThreadLocal<BaseWithPostConstructor>();

    @PostConstruct
    protected void init() {
        current.set(this);
    }

    static class Holder {
        @Inject BaseWithPostConstructor bean;
    }

    static void reset() {
        current.remove();
    }
}
