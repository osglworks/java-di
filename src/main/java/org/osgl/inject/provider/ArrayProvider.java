package org.osgl.inject.provider;

import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;
import org.osgl.inject.util.ArrayLoader;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

public class ArrayProvider implements Provider {
    protected final Class elementType;
    protected final BeanSpec listSpec;
    protected final Genie genie;


    private ArrayProvider(Class elementType, Genie genie) {
        this.elementType = elementType;
        this.listSpec = BeanSpec.of(ArrayList.class, null, genie);
        this.genie = genie;
    }

    private ArrayProvider(Class elementType, BeanSpec listSpec, Genie genie) {
        this.elementType = elementType;
        this.listSpec = listSpec;
        this.genie = genie;
    }

    @Override
    public Object get() {
        ArrayList list = genie.get(listSpec);
        return listToArray(list);
    }

    private Object listToArray(List list) {
        return ArrayLoader.listToArray(list, elementType);
    }

    public static ArrayProvider of(Class arrayClass, Genie genie) {
        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Array class expected");
        }
        return new ArrayProvider(arrayClass.getComponentType(), genie);
    }

    public static ArrayProvider of(BeanSpec beanSpec, Genie genie) {
        if (!beanSpec.isArray()) {
            throw new IllegalArgumentException("Array bean spec required");
        }
        Class arrayClass = beanSpec.rawType();
        return new ArrayProvider(arrayClass.getComponentType(), beanSpec.toList(), genie);
    }



}
