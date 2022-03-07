package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.rescaling.SimpleRescaler;
import muni.scrutiny.traces.models.Trace;

public class ScalingPipeline extends TracePipeline {
    public ScalingPipeline(double scale) {
        super(trace -> {
            SimpleRescaler sr = new SimpleRescaler(0,0, scale,0.0);
            return sr.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "Scaling";
    }

    @Override
    public String getMetricType() {
        return null;
    }
}
