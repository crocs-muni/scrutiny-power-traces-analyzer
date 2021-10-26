package muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor;

public class MeanProcessor implements ResamplingIntervalProcessor {
    @Override
    public double processInterval(double[] sequence, int start, int end) {
        double sum = 0d;
        for (int i = start; i <= end; i++) {
            sum += sequence[i];
        }

        return sum / (end - start + 1);
    }
}
