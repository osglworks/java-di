package org.osgl.inject;


import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

public class FibonacciSeriesHolder2 {

    @FibonacciSeries(max = 20)
    private int[] series;

    @Override
    public String toString() {
        return S.join(",", C.listOf(series));
    }
}
