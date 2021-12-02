package muni.scrutiny.module.pipelines.base;

import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;

import java.util.Map;

public class PipelineFactory {
    public static ComparisonPipeline getInstance(
            String pipelineName,
            int desiredSamplingFrequency,
            double referenceMinimum,
            double referenceMaximum,
            CustomPipelineParameters customParameters) {
        switch (pipelineName) {
            case PreprocessedEuclideanPipeline.name:
                return new PreprocessedEuclideanPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
            case PreprocessedCorrelationPipeline.name:
                return new PreprocessedCorrelationPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
            case PreprocessedDynamicWarpingPipeline.name:
                return new PreprocessedDynamicWarpingPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
            default:
                return new PreprocessedEuclideanPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
        }
    }
}
