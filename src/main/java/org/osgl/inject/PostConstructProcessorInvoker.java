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

import org.osgl.$;
import org.osgl.util.C;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.inject.Provider;

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
        return new PostConstructProcessorInvoker<>(realProvider, processors);
    }
}
