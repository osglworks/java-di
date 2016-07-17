package org.osgl.genie.builder;

import org.osgl.genie.Builder;
import org.osgl.util.C;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class SetBuilder extends CollectionBuilder<Set> {

    public SetBuilder(Class<Set> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected Set createInstance() {
        return C.newSet();
    }

    public static class Factory implements Builder.Factory<Set> {
        @Override
        public Builder<Set> createBuilder(Class<Set> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new SetBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<Set> targetClass() {
            return Set.class;
        }
    }
}
