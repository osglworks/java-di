package org.osgl.inject;

import org.osgl.$;
import org.osgl.util.C;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

class PostConstructProcessorInvoker<T> implements Provider<T> {

    private Provider<T> realProvider;
    private List<$.T2<Annotation, PostConstructProcessor<T>>> processors;

    private PostConstructProcessorInvoker(
            Provider<T> realProvider,
            List<$.T2<Annotation, PostConstructProcessor<T>>> processors
    ) {
        this.realProvider = realProvider;
        this.processors = processors;
    }

    @Override
    public T get() {
        T t = realProvider.get();
        for ($.T2<Annotation, PostConstructProcessor<T>> pair : processors) {
            pair._2.process(t, pair._1);
        }
        return t;
    }

    static <T> Provider<T> decorate(BeanSpec spec, Provider<T> realProvider, Genie genie) {
        if (realProvider instanceof PostConstructorInvoker) {
            return realProvider;
        }
        Set<Annotation> postProcessors = spec.postProcessors();
        if (postProcessors.isEmpty()) {
            return realProvider;
        }
        C.List<$.T2<Annotation, PostConstructProcessor<T>>> processors = C.newSizedList(postProcessors.size());
        for (Annotation annotation : postProcessors) {
            PostConstructProcessor<T> pcp = genie.postConstructProcessor(annotation);
            processors.add($.T2(annotation, pcp));
        }
        return new PostConstructProcessorInvoker<T>(realProvider, processors);
    }
}
