package org.osgl.inject;

import javax.inject.Inject;
import java.util.List;

public class ValueLoaderAndQualifiers {


    @Inject
    @FibonacciSeries
    @RandomList
    private List<Integer> list;

    public List<Integer> list() {
        return list;
    }

}
