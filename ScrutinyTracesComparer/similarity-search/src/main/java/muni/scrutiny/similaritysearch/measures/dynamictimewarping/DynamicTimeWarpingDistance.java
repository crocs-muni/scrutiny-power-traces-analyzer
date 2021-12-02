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

    public WarpPath getWarpingPath(double[] smallerVector, double[] biggerVector) {
        return FastDTW.getWarpPathBetween(new TimeSeries(smallerVector), new TimeSeries(biggerVector), 100, new EuclideanDistance());
    }
}