package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ChebyshevLowpassFilter;
import muni.scrutiny.traces.models.Trace;

public class ChebyshevFilterPipeline extends TracePipeline {
    public ChebyshevFilterPipeline(double cutoffFrequency) {
        super(trace -> {
            ChebyshevLowpassFilter wf = new ChebyshevLowpassFilter(cutoffFrequency);
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