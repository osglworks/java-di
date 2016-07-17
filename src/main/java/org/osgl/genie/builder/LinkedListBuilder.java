package org.osgl.genie.builder;

import org.osgl.genie.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LinkedListBuilder extends CollectionBuilder<LinkedList> {

    public LinkedListBuilder(Class<LinkedList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected LinkedList createInstance() {
        return new LinkedList();
    }

    public static class Factory implements Builder.Factory<LinkedList> {
        @Override
        public Builder<LinkedList> createBuilder(Class<LinkedList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new LinkedListBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<LinkedList> targetClass() {
            return LinkedList.class;
        }
    }
}
