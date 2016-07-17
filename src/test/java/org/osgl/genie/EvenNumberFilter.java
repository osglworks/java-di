package org.osgl.genie;

import org.osgl.Osgl;
import org.osgl.util.N;

import java.util.Map;

class EvenNumberFilter implements BeanFilter<Integer> {
    @Override
    public Osgl.Function<Integer, Boolean> filter(Object hint, Map<String, Object> options) {
        return N.F.IS_EVEN;
    }
}
