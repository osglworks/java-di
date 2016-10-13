package org.osgl.inject;


import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class EvenFibonacciSeriesHolder {

    @FibonacciSeries
    @EvenNumber
    List<Integer> series;

    @Override
    public String toString() {
        return S.join(",", series);
    }
}
