package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ChebyshevLowpassFilter;
import muni.scrutiny.traces.models.Trace;

public class TraceDFTPipeline extends TracePipeline {
    public TraceDFTPipeline() {
        super(trace -> {
            ChebyshevLowpassFilter wf = new ChebyshevLowpassFilter(10000d);
            return wf.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "DFT filter";
    }
}
