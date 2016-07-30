package org.osgl.inject;

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.inject.loader.ElementLoaderBase;
import org.osgl.util.C;
import org.osgl.util.N;

import java.util.List;
import java.util.Map;

class FibonacciSeriesLoader extends ElementLoaderBase<Integer> {

    @Override
    public List<Integer> load(Map<String, Object> options, BeanSpec container, Genie genie) {
        int max = toInt(options.get("max"));
        int n1 = 1, n2 = 1, f;
        List<Integer> list = C.newList();
        list.add(n1);
        list.add(n2);
        for (;;) {
            f = n1 + n2;
            n1 = n2;
            n2 = f;
            if (f < max) {
                list.add(f);
            } else {
                break;
            }
        }
        return list;
    }

    public boolean isFibonacciNumber(int n) {
        long l0 = 5 * n * n;
        return N.isPerfectSquare(l0 - 4) || N.isPerfectSquare(l0 + 4);
    }

    @Override
    public Osgl.Function<Integer, Boolean> filter(Map<String, Object> options, BeanSpec container) {
        final int max = toInt(options.get("max"));
        return new $.Predicate<Integer>() {
            @Override
            public boolean test(Integer n) {
                return n < max && isFibonacciNumber(n);
            }
        };
    }

    private static int toInt(Object max) {
        int n;
        if (max instanceof Number) {
            n = Math.abs(((Number) max).intValue());
        } else {
            n = Integer.parseInt(max.toString());
        }
        return n;
    }
}
