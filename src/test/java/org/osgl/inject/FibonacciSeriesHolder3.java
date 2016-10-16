package org.osgl.inject;


import org.osgl.inject.annotation.LoadCollection;
import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class FibonacciSeriesHolder3 {

    @LoadCollection(FibonacciSeriesLoader.class)
    private List<Integer> series;

    @Override
    public String toString() {
        return S.join(",", series);
    }
}
