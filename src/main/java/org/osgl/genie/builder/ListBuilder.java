package org.osgl.genie.builder;

import org.osgl.genie.Builder;
import org.osgl.util.C;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class ListBuilder extends CollectionBuilder<List> {

    public ListBuilder(Class<? extends List> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected List createInstance() {
        return C.newList();
    }

    public static class Factory implements Builder.Factory<List> {
        @Override
        public Builder<List> createBuilder(Class<List> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new ListBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<List> targetClass() {
            return List.class;
        }

    }

}
