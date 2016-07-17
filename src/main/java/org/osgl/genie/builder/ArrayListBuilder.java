package org.osgl.genie.builder;

import org.osgl.genie.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArrayListBuilder extends CollectionBuilder<ArrayList> {

    public ArrayListBuilder(Class<ArrayList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
        super(targetClass, annotations, typeParameters);
    }

    @Override
    protected ArrayList createInstance() {
        return new ArrayList();
    }

    public static class Factory implements Builder.Factory<ArrayList> {
        @Override
        public Builder<ArrayList> createBuilder(Class<ArrayList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            return new ArrayListBuilder(targetClass, annotations, typeParameters);
        }

        @Override
        public Class<ArrayList> targetClass() {
            return ArrayList.class;
        }
    }
}
