package org.osgl.inject.provider;

import org.osgl.inject.BeanSpec;
import org.osgl.inject.Genie;

import javax.inject.Provider;
import java.lang.reflect.Array;
import java.util.*;

public class ArrayProvider implements Provider {
    private Class elementType;
    private BeanSpec listSpec;
    private Genie genie;

    private static final DefaultArrayLoader DEF_LOADER = new DefaultArrayLoader();
    private static final Map<Class, ArrayLoader> loaders = new HashMap<Class, ArrayLoader>();
    static {
        loaders.put(Boolean.class, new BooleanArrayLoader());
        loaders.put(boolean.class, new BoolArrayLoader());
        loaders.put(Byte.class, new ByteArrayLoader());
        loaders.put(byte.class, new PrimitiveByteArrayLoader());
        loaders.put(Character.class, new CharacterArrayLoader());
        loaders.put(char.class, new CharArrayLoader());
        loaders.put(Short.class, new ShortArrayLoader());
        loaders.put(short.class, new PrimitiveShortArrayLoader());
        loaders.put(Integer.class, new IntegerArrayLoader());
        loaders.put(int.class, new IntArrayLoader());
        loaders.put(Float.class, new FloatArrayLoader());
        loaders.put(float.class, new PrimitiveFloatArrayLoader());
        loaders.put(Long.class, new LongArrayLoader());
        loaders.put(long.class, new PrimitiveLongArrayLoader());
        loaders.put(Double.class, new DoubleArrayLoader());
        loaders.put(double.class, new PrimitiveDoubleArrayLoader());
        loaders.put(String.class, new StringArrayLoader());
    }

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
        ArrayLoader loader = loaders.get(elementType);
        if (null == loader) {
            loader = DEF_LOADER;
        }
        return loader.loadFrom(list, elementType);
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

    private interface ArrayLoader<T> {
        T loadFrom(List list, Class elementType);
    }

    static class DefaultArrayLoader implements ArrayLoader {
        @Override
        public Object loadFrom(List list, Class elementType) {
            int sz = list.size();
            Object array = Array.newInstance(elementType, sz);
            for (int i = 0; i < sz; ++i) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }
    }

    private static class BoolArrayLoader implements ArrayLoader<boolean[]> {
        @Override
        public boolean[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            boolean[] a = new boolean[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Boolean) o;
            }
            return a;
        }
    }

    private static class BooleanArrayLoader implements ArrayLoader<Boolean[]> {
        @Override
        public Boolean[] loadFrom(List list, Class elementType) {
            return ((List<Boolean>) list).toArray(new Boolean[list.size()]);
        }
    }

    private static class PrimitiveByteArrayLoader implements ArrayLoader<byte[]> {
        @Override
        public byte[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            byte[] a = new byte[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Byte) o;
            }
            return a;
        }
    }

    private static class ByteArrayLoader implements ArrayLoader<Byte[]> {
        @Override
        public Byte[] loadFrom(List list, Class elementType) {
            return ((List<Byte>) list).toArray(new Byte[list.size()]);
        }
    }

    private static class CharArrayLoader implements ArrayLoader<char[]> {
        @Override
        public char[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            char[] a = new char[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Character) o;
            }
            return a;
        }
    }

    private static class CharacterArrayLoader implements ArrayLoader<Character[]> {
        @Override
        public Character[] loadFrom(List list, Class elementType) {
            return ((List<Character>) list).toArray(new Character[list.size()]);
        }
    }

    private static class PrimitiveShortArrayLoader implements ArrayLoader<short[]> {
        @Override
        public short[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            short[] a = new short[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Short) o;
            }
            return a;
        }
    }

    private static class ShortArrayLoader implements ArrayLoader<Short[]> {
        @Override
        public Short[] loadFrom(List list, Class elementType) {
            return ((List<Short>) list).toArray(new Short[list.size()]);
        }
    }

    private static class IntArrayLoader implements ArrayLoader<int[]> {
        @Override
        public int[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            int[] a = new int[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Integer)o;
            }
            return a;
        }
    }

    private static class IntegerArrayLoader implements ArrayLoader<Integer[]> {
        @Override
        public Integer[] loadFrom(List list, Class elementType) {
            return ((List<Integer>) list).toArray(new Integer[list.size()]);
        }
    }

    private static class PrimitiveFloatArrayLoader implements ArrayLoader<float[]> {
        @Override
        public float[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            float[] a = new float[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Float) o;
            }
            return a;
        }
    }

    private static class FloatArrayLoader implements ArrayLoader<Float[]> {
        @Override
        public Float[] loadFrom(List list, Class elementType) {
            return ((List<Float>) list).toArray(new Float[list.size()]);
        }
    }

    private static class PrimitiveLongArrayLoader implements ArrayLoader<long[]> {
        @Override
        public long[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            long[] a = new long[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Long) o;
            }
            return a;
        }
    }

    private static class LongArrayLoader implements ArrayLoader<Long[]> {
        @Override
        public Long[] loadFrom(List list, Class elementType) {
            return ((List<Long>) list).toArray(new Long[list.size()]);
        }
    }

    private static class PrimitiveDoubleArrayLoader implements ArrayLoader<double[]> {
        @Override
        public double[] loadFrom(List list, Class elementType) {
            int sz = list.size();
            double[] a = new double[sz];
            int i = 0;
            for (Object o: list) {
                a[i++] = (Double) o;
            }
            return a;
        }
    }

    private static class DoubleArrayLoader implements ArrayLoader<Double[]> {
        @Override
        public Double[] loadFrom(List list, Class elementType) {
            return ((List<Double>) list).toArray(new Double[list.size()]);
        }
    }

    private static class StringArrayLoader implements ArrayLoader<String[]> {
        @Override
        public String[] loadFrom(List list, Class elementType) {
            return ((List<String>) list).toArray(new String[list.size()]);
        }
    }

    private static class DateArrayLoader implements ArrayLoader<Date[]> {
        @Override
        public Date[] loadFrom(List list, Class elementType) {
            return ((List<Date>) list).toArray(new Date[list.size()]);
        }
    }

}
