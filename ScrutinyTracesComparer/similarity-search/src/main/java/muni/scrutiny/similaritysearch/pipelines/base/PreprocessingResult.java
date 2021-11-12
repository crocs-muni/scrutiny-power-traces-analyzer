package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.traces.models.Trace;

public class PreprocessingResult {
    private final Trace preprocessedTrace;

    public PreprocessingResult(Trace preprocessedTrace) {
        this.preprocessedTrace = preprocessedTrace;
    }

    public Trace getPreprocessedTrace() {
        return preprocessedTrace;
    }
}
