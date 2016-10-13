package org.osgl.inject;


import org.osgl.inject.annotation.Filter;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class OddFibonacciSeriesHolder {

    @FibonacciSeries
    @Filter(value = EvenNumberFilter.class, reverse = true)
    Integer[] series;

    @Override
    public String toString() {
        return S.join(",", C.listOf(series));
    }
}
