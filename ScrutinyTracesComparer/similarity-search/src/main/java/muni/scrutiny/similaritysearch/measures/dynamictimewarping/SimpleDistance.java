package muni.scrutiny.similaritysearch.measures.dynamictimewarping;

import smile.math.distance.Distance;

class SimpleDistance implements Distance<Double> {
    @Override
    public double d(Double aDouble, Double t1) {
        return Math.abs(aDouble - t1);
    }
}
