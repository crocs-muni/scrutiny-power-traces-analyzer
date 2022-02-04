package muni.scrutiny.module.pipelines.testing;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ButterworthLowpassFilter;
import muni.scrutiny.traces.models.Trace;

public class ButterworthFilterPipeline extends TracePipeline {
    public ButterworthFilterPipeline(double cutoffFrequency) {
        super(trace -> {
            ButterworthLowpassFilter wf = new ButterworthLowpassFilter(cutoffFrequency);
            return wf.preprocess(trace);
        });
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        return null;
    }

    @Override
    public String getName() {
        return "Butterworth filter";
    }
}
