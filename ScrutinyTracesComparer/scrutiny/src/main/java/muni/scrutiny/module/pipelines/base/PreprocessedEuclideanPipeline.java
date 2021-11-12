package muni.scrutiny.module.pipelines.base;

import muni.scrutiny.similaritysearch.measures.lnorm.EuclideanDistance;
import muni.scrutiny.similaritysearch.pipelines.slidingwindow.SlidingWindowTracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.LowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.similaritysearch.preprocessing.rescaling.SimpleRescaler;

public class PreprocessedEuclideanPipeline extends SlidingWindowTracePipeline {
    public PreprocessedEuclideanPipeline(
            int desiredSamplingFrequency,
            double referenceMaximum,
            double referenceMinimum) {
        super(new EuclideanDistance(),
                new TraceIntervalResampler(desiredSamplingFrequency, new MeanProcessor(), 1),
                new LowpassFilter(null),
                new SimpleOffsetNormalizer(referenceMinimum, referenceMaximum, null, null),
                new SimpleRescaler(referenceMinimum, referenceMaximum, null, null));
    }
}
