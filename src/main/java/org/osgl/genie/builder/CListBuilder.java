package org.osgl.genie.builder;

import org.osgl.genie.Builder;
import org.osgl.util.C;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class CListBuilder extends CollectionBuilder<C.List> {

    public CListBuilder(Class<? extends C.List> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected C.List createInstance() {
        return C.newList();
    }

    public static class Factory implements Builder.Factory<C.List> {
        @Override
        public Builder<C.List> createBuilder(Class<C.List> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new CListBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<C.List> targetClass() {
            return C.List.class;
        }

    }
}
