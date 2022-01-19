package muni.scrutiny.similaritysearch.measures.lnorm;

import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;

/**
 * Class for Euclidean metric function.
 * 
 * @author Martin
 */
public class EuclideanDistance implements DistanceMeasure {
    public EuclideanDistance() {
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        double sum = 0;
        for (int i = 0; i < smallerVector.length; i++) {
            sum += (smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i]) * (smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i]);
        }
        return Math.sqrt(sum);
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfSmallerVector, int firstIndexOfBiggerVector, int takeN) {
        double sum = 0;
        for (int i = 0; i < takeN; i++) {
            if (firstIndexOfSmallerVector + i > smallerVector.length - 1
                || firstIndexOfBiggerVector + i > biggerVector.length - 1) {
                break;
            }

            sum += (smallerVector[firstIndexOfSmallerVector + i] - biggerVector[firstIndexOfBiggerVector + i])
                    * (smallerVector[firstIndexOfSmallerVector + i] - biggerVector[firstIndexOfBiggerVector + i]);
        }

        return Math.sqrt(sum);
    }
}
