package org.osgl.genie;

import org.osgl.util.C;
import org.osgl.util.N;

import java.util.List;
import java.util.Map;

public class RandomListLoader implements ValueLoader<List<Integer>> {
    @Override
    public List<Integer> load(Map<String, Object> options, BeanSpec spec) {
        List<Integer> list = C.newList();
        for (int i = 0; i < 10; ++i) {
            list.add(N.randInt(100));
        }
        return list;
    }
}
