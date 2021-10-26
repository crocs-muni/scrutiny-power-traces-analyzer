package muni.scrutiny.similaritysearch.pipelines;

import muni.scrutiny.traces.models.Trace;

public interface PreprocessingFunction {
    Trace preprocess(Trace trace);
}
