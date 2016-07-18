package org.osgl.genie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

public class BuilderFactoryManagerTest extends TestBase {
    private static class ColBuilder extends Builder<AbstractCollection> {

        public ColBuilder(Class<? extends AbstractCollection> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            super(targetClass, annotations, typeParameters);
        }

        @Override
        protected AbstractCollection createInstance() {
            return new HashSet();
        }

        @Override
        protected void initializeInstance(AbstractCollection instance) {
            instance.add("ColBuilder");
        }

        static class Factory implements Builder.Factory<AbstractCollection> {
            @Override
            public Builder<AbstractCollection> createBuilder(Class<AbstractCollection> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
                return new ColBuilder(targetClass, annotations, typeParameters);
            }

            @Override
            public Class<AbstractCollection> targetClass() {
                return AbstractCollection.class;
            }
        }
    }

    private static class ListBuilder extends Builder<AbstractList> {

        public ListBuilder(Class<? extends AbstractList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
            super(targetClass, annotations, typeParameters);
        }

        @Override
        protected AbstractList createInstance() {
            return new ArrayList();
        }

        @Override
        protected void initializeInstance(AbstractList instance) {
            instance.add("ListBuilder");
        }

        static class Factory implements Builder.Factory<AbstractList> {
            @Override
            public Builder<AbstractList> createBuilder(Class<AbstractList> targetClass, Set<Annotation> annotations, List<Type> typeParameters) {
                return new ListBuilder(targetClass, annotations, typeParameters);
            }

            @Override
            public Class<AbstractList> targetClass() {
                return AbstractList.class;
            }
        }
    }

    @Before
    public void setup() {
        Builder.Factory.Manager.destroy();
        Builder.Factory.Manager.register(new ColBuilder.Factory());
        Builder.Factory.Manager.register(new ListBuilder.Factory());
    }

    @Test
    public void ColListBuilderShallTakePrecedenceOfListBuilderForCollection() {
        Builder.Factory<Collection> factory = Builder.Factory.Manager.get(Collection.class);
        assertNotNull(factory);
        Builder<Collection> builder = factory.createBuilder(Collection.class, null, null);
        Collection c = builder.get();
        yes(c instanceof Set);
    }

    @Test
    public void ListBuilderShallBeUsedForListInjection() {
        Builder.Factory<List> factory = Builder.Factory.Manager.get(List.class);
        assertNotNull(factory);
        Builder<List> builder = factory.createBuilder(List.class, null, null);
        List l = builder.get();
        assertNotNull(l);
        eq("ListBuilder", l.get(0));
    }
}
