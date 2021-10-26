package muni.scrutiny.similaritysearch.preprocessing.resampling;

import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.ResamplingIntervalProcessor;
import muni.scrutiny.traces.models.Trace;

public class TraceIntervalResampler implements Resampler {
    private final Trace traceToResample;
    private final int desiredSamplingFrequency;
    private final ResamplingIntervalProcessor processor;
    private final int intervalRadius;

    public TraceIntervalResampler(
            Trace traceToResample,
            int desiredSamplingFrequency,
            ResamplingIntervalProcessor processor,
            int intervalRadius) {
        this.traceToResample = traceToResample;
        this.desiredSamplingFrequency = desiredSamplingFrequency;
        this.processor = processor;
        this.intervalRadius = intervalRadius;
    }

    public Trace resample() {
        double samplingCoeff = (double)desiredSamplingFrequency/traceToResample.getSamplingFrequency();
        double inverseSamplingCoeff = (double)traceToResample.getSamplingFrequency()/desiredSamplingFrequency;

        if (samplingCoeff == 1) {
            return new Trace(traceToResample);
        }

        double[] resampledArray = resampleByIntervals(samplingCoeff, inverseSamplingCoeff);
        return new Trace(
                traceToResample.getName(),
                resampledArray.length,
                traceToResample.getVoltageUnit(),
                traceToResample.getTimeUnit(),
                resampledArray,
                desiredSamplingFrequency);
    }

    private double[] resampleByIntervals(double samplingCoeff, double inverseSamplingCoeff) {
        int ttrSize = traceToResample.getDataCount();
        int resultingSize = (int)(ttrSize * samplingCoeff);
        double[] resampledArray = new double[resultingSize];
        for (int i = 0; i < resultingSize; i++) {
            int mid = (int) Math.ceil(i * inverseSamplingCoeff);
            int start = Math.max(Math.min(mid - intervalRadius, ttrSize - 1), 0);
            int end = Math.min(mid + intervalRadius, ttrSize - 1);
            resampledArray[i] = processor.processInterval(traceToResample.getVoltage(), start, end);
        }

        return resampledArray;
    }
}
