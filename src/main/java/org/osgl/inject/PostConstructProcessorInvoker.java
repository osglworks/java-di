package org.osgl.inject;

import org.osgl.$;
import org.osgl.util.E;

import javax.inject.Provider;

class PostConstructProcessorInvoker<T> implements Provider<T> {

    private Provider<T> realProvider;
    private PostConstructProcessor<T>[] processores;

    private PostConstructProcessorInvoker(Provider<T> realProvider) {
        this.realProvider = realProvider;
    }

    @Override
    public T get() {
        T t = realProvider.get();
        for (PostConstructProcessor<T> processor : processores) {
            processor.apply(t);
        }
        return t;
    }

    static <T> Provider<T> decorate(BeanSpec spec, Provider<T> realProvider, Genie genie) {
        if (realProvider instanceof PostConstructorInvoker) {
            return realProvider;
        }
        throw E.tbd();
    }
}
