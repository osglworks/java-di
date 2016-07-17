package org.osgl.genie;


import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class EvenFibonacciSeriesHolder {

    @Inject
    @FibonacciSeries
    @EvenNumber
    List<Integer> series;

    @Override
    public String toString() {
        return S.join(",", series);
    }
}
