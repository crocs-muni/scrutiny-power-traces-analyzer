package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;

public abstract class TracePipeline<TComparisonResult extends ComparisonResult> implements ComparisonPipeline<PreprocessingResult, TComparisonResult> {
    public final Preprocessor[] preprocessors;
    protected Trace preprocessedTrace;
    protected boolean isTracePreprocessed;

    public TracePipeline(Preprocessor... preprocessors) {
        this.preprocessors = preprocessors;
        this.preprocessedTrace = null;
        this.isTracePreprocessed = false;
    }

    @Override
    public PreprocessingResult preprocess(Trace trace) {
        Trace tmpTrace = trace;
        for (Preprocessor p : preprocessors) {
            tmpTrace = p.preprocess(tmpTrace);
        }

        isTracePreprocessed = true;
        preprocessedTrace = tmpTrace;
        return new PreprocessingResult(tmpTrace);
    }
}
