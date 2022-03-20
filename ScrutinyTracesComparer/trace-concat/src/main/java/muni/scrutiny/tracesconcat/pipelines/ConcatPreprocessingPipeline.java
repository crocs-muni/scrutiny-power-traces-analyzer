package muni.scrutiny.tracesconcat.pipelines;

import muni.scrutiny.similaritysearch.measures.lnorm.EuclideanDistance;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ButterworthLowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.traces.models.Trace;

public class ConcatPreprocessingPipeline extends TracePipeline {
    public static final String name = "cpp";

    public ConcatPreprocessingPipeline(int desiredSamplingFrequency, double offset) {
        super(new TraceIntervalResampler(desiredSamplingFrequency, new MeanProcessor(), 1),
                new ButterworthLowpassFilter(null),
                new SimpleOffsetNormalizer(0, 0, offset, null));
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMetricType() {
        return null;
    }
}
