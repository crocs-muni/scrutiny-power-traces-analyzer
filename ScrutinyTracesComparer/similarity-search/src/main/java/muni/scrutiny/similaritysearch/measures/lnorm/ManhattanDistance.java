package muni.scrutiny.similaritysearch.measures.lnorm;

import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;

/**
 * Class for Manhattan metric function.
 * 
 * @author Martin
 */
public class ManhattanDistance implements DistanceMeasure {
    public ManhattanDistance() {
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        double sum = 0;
        for (int i = 0; i < smallerVector.length; i++) {
            sum += Math.abs(smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i]);
        }
        return sum;
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfSmallerVector, int firstIndexOfBiggerVector, int takeN) {
        double sum = 0;
        for (int i = firstIndexOfSmallerVector; i < Math.min(firstIndexOfSmallerVector + takeN, smallerVector.length - 1); i++) {
            if (firstIndexOfSmallerVector + i > smallerVector.length - 1
                    || firstIndexOfBiggerVector + i > biggerVector.length - 1) {
                break;
            }

            sum += Math.abs(smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i]);
        }
        return sum;
    }
}
