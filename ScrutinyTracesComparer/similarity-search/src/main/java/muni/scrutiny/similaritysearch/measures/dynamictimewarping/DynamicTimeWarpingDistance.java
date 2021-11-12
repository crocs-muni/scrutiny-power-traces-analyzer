package muni.scrutiny.similaritysearch.measures.dynamictimewarping;

import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;
import smile.math.distance.DynamicTimeWarping;

public class DynamicTimeWarpingDistance implements DistanceMeasure {
    public int DEFAULT_RADIUS = 100;
    private final int radius;

    public DynamicTimeWarpingDistance() {
        this.radius = DEFAULT_RADIUS;
    }

    public DynamicTimeWarpingDistance(int radius) {
        this.radius = radius;
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        DynamicTimeWarping dtw = new DynamicTimeWarping(new SimpleDistance(), radius);
        return dtw.d(smallerVector, biggerVector);
    }
}