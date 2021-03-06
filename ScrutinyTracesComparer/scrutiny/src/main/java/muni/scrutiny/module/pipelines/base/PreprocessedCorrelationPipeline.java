package muni.scrutiny.module.pipelines.base;

import muni.scrutiny.similaritysearch.measures.samplecrosscorellation.SampleCrossCorellationDistance;
import muni.scrutiny.similaritysearch.pipelines.base.CorrelationSlidingWindowTracePipeline;
import muni.scrutiny.similaritysearch.pipelines.base.SlidingWindowTracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ButterworthLowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.similaritysearch.preprocessing.rescaling.SimpleRescaler;

public class PreprocessedCorrelationPipeline extends CorrelationSlidingWindowTracePipeline {
    public static final String name = "pcp";

    public PreprocessedCorrelationPipeline(
            int desiredSamplingFrequency,
            double referenceMinimum,
            double referenceMaximum,
            CustomPipelineParameters customParameters) {
        super(new SampleCrossCorellationDistance(),
                new TraceIntervalResampler(desiredSamplingFrequency, new MeanProcessor(), 1),
                new ButterworthLowpassFilter(customParameters == null ? null :customParameters.getDoubleParameter("cutoffFrequency")),
                new SimpleOffsetNormalizer(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("offset"), customParameters == null ? null : customParameters.getDoubleParameter("normalizerInvervalCoefficient")),
                new SimpleRescaler(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("scale"), customParameters == null ? null : customParameters.getDoubleParameter("scalerInvervalCoefficient")));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMetricType() { return "correlation"; }
}
