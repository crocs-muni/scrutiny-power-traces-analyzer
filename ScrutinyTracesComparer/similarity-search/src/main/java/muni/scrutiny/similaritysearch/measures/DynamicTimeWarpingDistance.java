package muni.scrutiny.similaritysearch.measures;

import smile.math.distance.Distance;
import smile.math.distance.DynamicTimeWarping;

public class DynamicTimeWarpingDistance {
    public double compute(double[] smallerVector, double[] biggerVector) {
        DynamicTimeWarping dtw = new DynamicTimeWarping(new SimpleDistance(), 100);
        return dtw.d(smallerVector, biggerVector);
    }
}

class SimpleDistance implements Distance<Double> {
    @Override
    public double d(Double aDouble, Double t1) {
        return Math.abs(aDouble - t1);
    }
}