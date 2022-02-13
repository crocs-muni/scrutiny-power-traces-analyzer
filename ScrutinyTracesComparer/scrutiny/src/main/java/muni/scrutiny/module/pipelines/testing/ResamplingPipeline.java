package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.traces.models.Trace;

public class ResamplingPipeline extends TracePipeline {
    public ResamplingPipeline(int desiredSamplingFrequency, int intervalRadius) {
        super(trace -> {
            TraceIntervalResampler tir = new TraceIntervalResampler(
                    desiredSamplingFrequency,
                    (sequence, start, end) -> new MeanProcessor().processInterval(sequence, start, end),
                    intervalRadius);
            return tir.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "resampling";
    }

    @Override
    public String getMetricType() {
        return null;
    }
}
