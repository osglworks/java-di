package org.osgl.genie.builder;

import org.osgl.genie.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TreeSetBuilder extends CollectionBuilder<TreeSet> {

    public TreeSetBuilder(Class<TreeSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected TreeSet createInstance() {
        return new TreeSet();
    }

    public static class Factory implements Builder.Factory<TreeSet> {
        @Override
        public Builder<TreeSet> createBuilder(Class<TreeSet> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new TreeSetBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<TreeSet> targetClass() {
            return TreeSet.class;
        }
    }
}
