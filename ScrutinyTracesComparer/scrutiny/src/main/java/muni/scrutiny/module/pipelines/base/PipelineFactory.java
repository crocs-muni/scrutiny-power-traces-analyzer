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
            case "pep":
                return new PreprocessedEuclideanPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
            default:
                return new PreprocessedEuclideanPipeline(desiredSamplingFrequency, referenceMinimum, referenceMaximum, customParameters);
        }
    }
}
