package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;

public abstract class TracePipeline<TComparisonResult extends ComparisonResult> implements ComparisonPipeline<PreprocessingResult, TComparisonResult> {
    protected final Preprocessor[] preprocessors;

    public TracePipeline(Preprocessor... preprocessors) {
        this.preprocessors = preprocessors;
    }

    @Override
    public PreprocessingResult preprocess(Trace trace) {
        Trace tmpTrace = trace;
        for (Preprocessor p : preprocessors) {
            tmpTrace = p.preprocess(tmpTrace);
        }

        return new PreprocessingResult(tmpTrace);
    }
}
