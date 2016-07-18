package org.osgl.genie.builder;

import org.osgl.genie.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LinkedHashSetBuilder extends CollectionBuilder<LinkedHashSet> {

    public LinkedHashSetBuilder(Class<LinkedHashSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected LinkedHashSet createInstance() {
        return new LinkedHashSet();
    }

    public static class Factory implements Builder.Factory<LinkedHashSet> {
        @Override
        public Builder<LinkedHashSet> createBuilder(Class<LinkedHashSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new LinkedHashSetBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<LinkedHashSet> targetClass() {
            return LinkedHashSet.class;
        }
    }
}
