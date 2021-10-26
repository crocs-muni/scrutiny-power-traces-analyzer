package muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor;

public interface ResamplingIntervalProcessor {
    double processInterval(double[] sequence, int start, int end);
}
