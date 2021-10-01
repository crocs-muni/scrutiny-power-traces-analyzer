package muni.scrutiny.similaritysearch.measures;

public class InfinityNormDistance {
    public double compute(double[] smallerVector, double[] biggerVector, int firstIndexOfBiggerVector) {
        double max = 0;
        for (int i = 0; i < smallerVector.length; i++) {
            double dist = smallerVector[i] - biggerVector[firstIndexOfBiggerVector + i];
            if (dist > max) {
                max = dist;
            }
        }

        return max;
    }
}
