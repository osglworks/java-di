package org.osgl.inject.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class AnnotationUtil {
    public static <T extends Annotation> T declaredAnnotation(Class c, Class<T> annoClass) {
        Annotation[] aa = c.getDeclaredAnnotations();
        if (null == aa) {
            return null;
        }
        for (Annotation a : aa) {
            if (annoClass.isInstance(a)) {
                return (T) a;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Annotation} tagged on another annotation instance
     *
     * @param annotation the annotation instance
     * @param tagClass   the expected annotation class
     * @param <T>        the generic type of the expected annotation
     * @return the annotation tagged on annotation of type `tagClass`
     */
    public static <T extends Annotation> T tagAnnotation(Annotation annotation, Class<T> tagClass) {
        Class<?> c = annotation.annotationType();
        for (Annotation a : c.getAnnotations()) {
            if (tagClass.isInstance(a)) {
                return (T) a;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotation(final Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz, Annotation.class}, new SimpleAnnoInvocationHandler(clazz));
    }

    /**
     * <p>Generate a hash code for the given annotation using the algorithm
     * presented in the {@link Annotation#hashCode()} API docs.</p>
     *
     * @param a the Annotation for a hash code calculation is desired, not
     *          {@code null}
     * @return the calculated hash code
     * @throws RuntimeException      if an {@code Exception} is encountered during
     *                               annotation member access
     * @throws IllegalStateException if an annotation method invocation returns
     *                               {@code null}
     */
    public static int hashCode(Annotation a) {
        int result = 0;
        Class<? extends Annotation> type = a.annotationType();
        for (Method m : type.getDeclaredMethods()) {
            try {
                Object value = m.invoke(a);
                if (value == null) {
                    throw new IllegalStateException(
                            String.format("Annotation method %s returned null", m));
                }
                result += hashMember(m.getName(), value);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }

    /**
     * Helper method for generating a hash code for a member of an annotation.
     *
     * @param name  the name of the member
     * @param value the value of the member
     * @return a hash code for this member
     */
    public static int hashMember(String name, Object value) {
        int part1 = name.hashCode() * 127;
        if (value.getClass().isArray()) {
            return part1 ^ arrayMemberHash(value.getClass().getComponentType(), value);
        }
        if (value instanceof Annotation) {
            return part1 ^ hashCode((Annotation) value);
        }
        return part1 ^ value.hashCode();
    }

    /**
     * Helper method for generating a hash code for an array.
     *
     * @param componentType the component type of the array
     * @param o             the array
     * @return a hash code for the specified array
     */
    private static int arrayMemberHash(Class<?> componentType, Object o) {
        if (componentType.equals(Byte.TYPE)) {
            return Arrays.hashCode((byte[]) o);
        }
        if (componentType.equals(Short.TYPE)) {
            return Arrays.hashCode((short[]) o);
        }
        if (componentType.equals(Integer.TYPE)) {
            return Arrays.hashCode((int[]) o);
        }
        if (componentType.equals(Character.TYPE)) {
            return Arrays.hashCode((char[]) o);
        }
        if (componentType.equals(Long.TYPE)) {
            return Arrays.hashCode((long[]) o);
        }
        if (componentType.equals(Float.TYPE)) {
            return Arrays.hashCode((float[]) o);
        }
        if (componentType.equals(Double.TYPE)) {
            return Arrays.hashCode((double[]) o);
        }
        if (componentType.equals(Boolean.TYPE)) {
            return Arrays.hashCode((boolean[]) o);
        }
        return Arrays.hashCode((Object[]) o);
    }
}
