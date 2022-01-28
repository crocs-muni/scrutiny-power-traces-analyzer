package muni.scrutiny.similaritysearch.measures.dynamictimewarping;

import com.dtw.FastDTW;
import com.dtw.WarpPath;
import com.timeseries.TimeSeries;
import com.util.EuclideanDistance;
import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;

public class DynamicTimeWarpingDistance implements DistanceMeasure {
    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        return FastDTW.getWarpDistBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), 100, new EuclideanDistance());
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfSmallerVector, int firstIndexOfBiggerVector, int takeN) {
        return FastDTW.getWarpDistBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), 100, new EuclideanDistance());
    }

    @Override
    public double getWorstSimilarity() {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isBetterSimilarity(double currentSimilarity, double newSimilarity) {
        return currentSimilarity > newSimilarity;
    }

    public WarpPath getWarpingPath(double[] smallerVector, double[] biggerVector) {
        return FastDTW.getWarpPathBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), 100, new EuclideanDistance());
    }
}