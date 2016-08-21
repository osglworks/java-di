package org.osgl.inject;

import org.osgl.util.C;
import org.osgl.util.N;

import java.util.List;
import java.util.Map;

public class RandomListLoader extends ValueLoader.Base<List<Integer>> {

    @Override
    public List<Integer> get() {
        List<Integer> list = C.newList();
        for (int i = 0; i < 10; ++i) {
            list.add(N.randInt(100));
        }
        return list;
    }

}
