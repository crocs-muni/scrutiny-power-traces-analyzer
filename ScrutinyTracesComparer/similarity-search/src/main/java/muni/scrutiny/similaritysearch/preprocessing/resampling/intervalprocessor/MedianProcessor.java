package muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor;

public class MedianProcessor implements ResamplingIntervalProcessor {
    @Override
    public double processInterval(double[] sequence, int start, int end) {
        int n = end - start + 1;
        double m = 0;
        if (n % 2 == 1)
        {
            m = sequence[(n + 1) / 2 - 1];
        }
        else
        {
            m = (sequence[n / 2 - 1] + sequence[n / 2]) / 2;
        }

        return m;
    }
}
