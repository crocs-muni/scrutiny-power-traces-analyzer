package muni.scrutiny.similaritysearch.measures;

public class ShortTimeSeriesDistance {
    public double compute(
            double[] smallerVector,
            double[] biggerVector,
            double[] smallerVectorTime,
            double[] biggerVectorTime,
            int firstIndexOfBiggerVector) {
        double sum = 0;
        for (int i = 0; i < smallerVector.length - 1; i++) {
            sum += Math.pow((biggerVector[firstIndexOfBiggerVector + i + 1] - biggerVector[firstIndexOfBiggerVector + i])
                        /(smallerVectorTime[i + 1] - smallerVectorTime[i])
                -(smallerVectorTime[i + 1] - smallerVectorTime[i])
                        /(biggerVectorTime[firstIndexOfBiggerVector + i + 1] - biggerVectorTime[firstIndexOfBiggerVector + i]), 2);
        }

        return Math.sqrt(sum);
    }
}
