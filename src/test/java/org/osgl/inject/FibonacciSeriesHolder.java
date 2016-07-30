package org.osgl.inject;


import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class FibonacciSeriesHolder {

    @Inject
    @FibonacciSeries(max = 20)
    private List<Integer> series;

    @Override
    public String toString() {
        return S.join(",", series);
    }
}
