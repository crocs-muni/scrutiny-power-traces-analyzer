package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.traces.models.Trace;

public interface ComparisonPipeline<TPreprocessingResult extends PreprocessingResult, TComaprisonResult extends ComparisonResult> {
    TPreprocessingResult preprocess(Trace traceToPreprocess);
    TComaprisonResult compare(Trace referenceTrace, Trace preprocessedTrace);
    String getName();
}