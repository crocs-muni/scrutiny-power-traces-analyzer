package muni.scrutiny.similaritysearch.measures;

public class MinkowskiDistance {
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector, double p) {
        double sum = 0;
        for (int i = 0; i < smallerVector.length; i++) {
            sum += Math.pow(smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i], p);
        }

        return Math.pow(sum, 1d/p);
    }
}
