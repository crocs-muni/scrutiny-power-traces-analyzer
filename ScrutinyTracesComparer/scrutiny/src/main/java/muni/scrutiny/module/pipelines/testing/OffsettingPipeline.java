package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.traces.models.Trace;

public class OffsettingPipeline extends TracePipeline {
    public OffsettingPipeline(double offset) {
        super(trace -> {
            SimpleOffsetNormalizer son = new SimpleOffsetNormalizer(0,0, offset,0.0);
            return son.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "Offsetting";
    }

    @Override
    public String getMetricType() {
        return null;
    }
}
