package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.BesselLowpassFilter;
import muni.scrutiny.traces.models.Trace;

public class BesselFilterPipeline extends TracePipeline {
    public BesselFilterPipeline(double cutoffFrequency) {
        super(trace -> {
            BesselLowpassFilter wf = new BesselLowpassFilter(cutoffFrequency);
            return wf.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "Chebyshev filter";
    }
}