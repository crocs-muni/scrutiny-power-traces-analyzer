package muni.scrutiny.similaritysearch.measures;

/**
 * https://www.sciencedirect.com/topics/earth-and-planetary-sciences/cross-correlation
 */
public class CrossCorellationDistance {
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector, double p) {
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
}