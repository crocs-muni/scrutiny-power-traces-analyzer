package muni.scrutiny.similaritysearch.preprocessing.resampling;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.ResamplingIntervalProcessor;
import muni.scrutiny.traces.models.Trace;

public class TraceIntervalResampler implements Preprocessor {
    private final int desiredSamplingFrequency;
    private final ResamplingIntervalProcessor processor;
    private final int intervalRadius;

    public TraceIntervalResampler(
            int desiredSamplingFrequency,
            ResamplingIntervalProcessor processor,
            int intervalRadius) {
        this.desiredSamplingFrequency = desiredSamplingFrequency;
        this.processor = processor;
        this.intervalRadius = intervalRadius;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        double samplingCoeff = (double)desiredSamplingFrequency/ traceToPreprocess.getSamplingFrequency();
        double inverseSamplingCoeff = (double) traceToPreprocess.getSamplingFrequency()/desiredSamplingFrequency;

        if (samplingCoeff == 1) {
            return new Trace(traceToPreprocess);
        }

        double[] resampledArray = resampleByIntervals(traceToPreprocess, samplingCoeff, inverseSamplingCoeff);
        return new Trace(
                traceToPreprocess.getName(),
                resampledArray.length,
                traceToPreprocess.getVoltageUnit(),
                traceToPreprocess.getTimeUnit(),
                resampledArray,
                desiredSamplingFrequency);
    }

    private double[] resampleByIntervals(Trace traceToPreprocess, double samplingCoeff, double inverseSamplingCoeff) {
        int ttrSize = traceToPreprocess.getDataCount();
        int resultingSize = (int)(ttrSize * samplingCoeff);
        double[] resampledArray = new double[resultingSize];
        for (int i = 0; i < resultingSize; i++) {
            int mid = (int) Math.ceil(i * inverseSamplingCoeff);
            int start = Math.max(Math.min(mid - intervalRadius, ttrSize - 1), 0);
            int end = Math.min(mid + intervalRadius, ttrSize - 1);
            resampledArray[i] = processor.processInterval(traceToPreprocess.getVoltage(), start, end);
        }

        return resampledArray;
    }
}
