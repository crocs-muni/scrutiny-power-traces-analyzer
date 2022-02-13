package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ExponentialSmoother;
import muni.scrutiny.traces.models.Trace;

public class ExponentialSmootherPipeline extends TracePipeline {
    public ExponentialSmootherPipeline(Double alpha) {
        super(trace -> {
            ExponentialSmoother es = new ExponentialSmoother(alpha);
            return es.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "Exponential smoother";
    }

    @Override
    public String getMetricType() {
        return null;
    }
}
