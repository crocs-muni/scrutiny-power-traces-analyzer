package muni.scrutiny.similaritysearch.pipelines;

import muni.scrutiny.traces.models.Trace;

public class TracePipeline {
    public final PreprocessingFunction[] preprocessingFunctions;
    private Trace preprocessedTrace;
    private boolean isTracePreprocessed;

    public TracePipeline(PreprocessingFunction... preprocessingFunctions) {
        this.preprocessingFunctions = preprocessingFunctions;
        this.preprocessedTrace = null;
        this.isTracePreprocessed = false;
    }

    public Trace preprocess(Trace trace) {
        Trace tmpTrace = trace;
        for (PreprocessingFunction pf : preprocessingFunctions) {
            tmpTrace = pf.preprocess(tmpTrace);
        }

        isTracePreprocessed = true;
        preprocessedTrace = tmpTrace;
        return tmpTrace;
    }

    public double process(Trace trace) {
        if (isTracePreprocessed) {
            return 0;
        }

        return 0;
    }
}
