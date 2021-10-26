package muni.scrutiny.testinggui.testingpipelines;

import muni.scrutiny.similaritysearch.pipelines.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;

public class TraceResamplingPipeline extends TracePipeline {
    public TraceResamplingPipeline(int desiredSamplingFrequency, int intervalRadius) {
        super(trace -> {
            TraceIntervalResampler tir = new TraceIntervalResampler(
                    trace,
                    desiredSamplingFrequency,
                    (sequence, start, end) -> new MeanProcessor().processInterval(sequence, start, end),
                    intervalRadius);
            return tir.resample();
        });
    }
}
