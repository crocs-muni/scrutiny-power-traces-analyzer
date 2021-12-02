package muni.scrutiny.module.pipelines.base;

import muni.scrutiny.similaritysearch.measures.lnorm.EuclideanDistance;
import muni.scrutiny.similaritysearch.pipelines.slidingwindow.SlidingWindowTracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.LowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.similaritysearch.preprocessing.rescaling.SimpleRescaler;

import java.util.List;

public class PreprocessedEuclideanPipeline extends SlidingWindowTracePipeline {
    public static final String name = "pep";

    public PreprocessedEuclideanPipeline(
            int desiredSamplingFrequency,
            double referenceMinimum,
            double referenceMaximum,
            CustomPipelineParameters customParameters) {
        super(new EuclideanDistance(),
                new TraceIntervalResampler(desiredSamplingFrequency, new MeanProcessor(), 1),
                new LowpassFilter(customParameters == null ? null :customParameters.getDoubleParameter("cutoffFrequency")),
                new SimpleOffsetNormalizer(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("offset"), customParameters == null ? null :customParameters.getDoubleParameter("normalizerInvervalCoefficient")),
                new SimpleRescaler(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("scale"), customParameters == null ? null : customParameters.getDoubleParameter("scalerInvervalCoefficient")));
    }

    @Override
    public String getName() {
        return name;
    }
}
