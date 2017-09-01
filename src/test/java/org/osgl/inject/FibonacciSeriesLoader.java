package org.osgl.inject;

/*-
 * #%L
 * OSGL Genie
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.inject.loader.ElementLoaderBase;
import org.osgl.util.C;
import org.osgl.util.N;

import java.util.List;
import java.util.Map;

class FibonacciSeriesLoader extends ElementLoaderBase<Integer> {

    private static final int DEF_MAX = 10;

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
        if (null == max) {
            return DEF_MAX;
        }
        int n;
        if (max instanceof Number) {
            n = Math.abs(((Number) max).intValue());
        } else {
            n = Integer.parseInt(max.toString());
        }
        return n;
    }
}
