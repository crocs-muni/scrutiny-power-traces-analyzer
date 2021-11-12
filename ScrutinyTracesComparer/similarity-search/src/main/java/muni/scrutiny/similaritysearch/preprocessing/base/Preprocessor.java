package muni.scrutiny.similaritysearch.preprocessing.base;

import muni.scrutiny.traces.models.Trace;

public interface Preprocessor {
    Trace preprocess(Trace traceToPreprocess);
}
