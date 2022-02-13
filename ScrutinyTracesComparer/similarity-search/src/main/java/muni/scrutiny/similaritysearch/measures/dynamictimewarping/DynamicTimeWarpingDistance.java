package muni.scrutiny.similaritysearch.measures.dynamictimewarping;

import com.dtw.FastDTW;
import com.dtw.WarpPath;
import com.timeseries.TimeSeries;
import com.util.EuclideanDistance;
import muni.scrutiny.similaritysearch.measures.base.SimilarityMeasure;

public class DynamicTimeWarpingDistance implements SimilarityMeasure {
    public static final int DEFAULT_RADIUS = 50;

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        return FastDTW.getWarpDistBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), DEFAULT_RADIUS, new EuclideanDistance());
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfSmallerVector, int firstIndexOfBiggerVector, int takeN) {
        return FastDTW.getWarpDistBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), DEFAULT_RADIUS, new EuclideanDistance());
    }

    public WarpPath getWarpingPath(double[] smallerVector, double[] biggerVector) {
        return FastDTW.getWarpPathBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), DEFAULT_RADIUS, new EuclideanDistance());
    }
}