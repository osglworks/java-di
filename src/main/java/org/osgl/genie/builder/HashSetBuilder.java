package org.osgl.genie.builder;

import org.osgl.genie.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HashSetBuilder extends CollectionBuilder<HashSet> {

    public HashSetBuilder(Class<HashSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected HashSet createInstance() {
        return new HashSet();
    }

    public static class Factory implements Builder.Factory<HashSet> {
        @Override
        public Builder<HashSet> createBuilder(Class<HashSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new HashSetBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<HashSet> targetClass() {
            return HashSet.class;
        }
    }
}
