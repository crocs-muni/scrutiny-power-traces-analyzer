package muni.scrutiny.similaritysearch.measures.crosscorellation;

import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;

/**
 * https://www.sciencedirect.com/topics/earth-and-planetary-sciences/cross-correlation
 */
public class CrossCorellationDistance implements DistanceMeasure {
    public CrossCorellationDistance() {
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        double sum1 = 0;
        double sum2 = 0;
        for (int i = 0; i < smallerVector.length; i++) {
            sum1 += smallerVector[i];
        }

        for (int i = 0; i < smallerVector.length; i++) {
            sum2 += biggerVector[firstIndexOfBiggerVector + i];
        }

        double average1 = sum1 / smallerVector.length;
        double average2 = sum2 / smallerVector.length;

        double covariance = 0;
        double variance1 = 0;
        double variance2 = 0;

        for (int i = 0; i < smallerVector.length; i++) {
            covariance += (smallerVector[i] - average1) * (biggerVector[firstIndexOfBiggerVector + i] - average2);
            variance1 += Math.pow(smallerVector[i] - average1, 2);
            variance2 += Math.pow(biggerVector[firstIndexOfBiggerVector + i] - average2, 2);
        }

        return covariance/Math.sqrt(variance1*variance2);
    }

    @Override
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfSmallerVector, int firstIndexOfBiggerVector, int takeN) {
        double sum1 = 0;
        double sum2 = 0;
        for (int i = 0; i < takeN; i++) {
            if (firstIndexOfSmallerVector + i > smallerVector.length - 1
                    || firstIndexOfBiggerVector + i > biggerVector.length - 1) {
                break;
            }

            sum1 += smallerVector[i];
        }

        for (int i = 0; i < takeN; i++) {
            if (firstIndexOfSmallerVector + i > smallerVector.length - 1
                    || firstIndexOfBiggerVector + i > biggerVector.length - 1) {
                break;
            }

            sum2 += biggerVector[firstIndexOfBiggerVector + i];
        }

        double average1 = sum1 / smallerVector.length;
        double average2 = sum2 / smallerVector.length;

        double covariance = 0;
        double variance1 = 0;
        double variance2 = 0;

        for (int i = 0; i < takeN; i++) {
            if (firstIndexOfSmallerVector + i > smallerVector.length - 1
                    || firstIndexOfBiggerVector + i > biggerVector.length - 1) {
                break;
            }

            covariance += (smallerVector[i] - average1) * (biggerVector[firstIndexOfBiggerVector + i] - average2);
            variance1 += Math.pow(smallerVector[i] - average1, 2);
            variance2 += Math.pow(biggerVector[firstIndexOfBiggerVector + i] - average2, 2);
        }

        return covariance/Math.sqrt(variance1*variance2);
    }

    @Override
    public double getWorstSimilarity() {
        return Double.MIN_VALUE;
    }

    @Override
    public boolean isBetterSimilarity(double currentSimilarity, double newSimilarity) {
        return currentSimilarity < newSimilarity;
    }
}
