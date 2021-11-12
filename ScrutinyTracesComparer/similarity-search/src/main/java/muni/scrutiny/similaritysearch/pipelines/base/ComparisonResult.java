package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.traces.models.Trace;

public class ComparisonResult {
    private final Trace preprocessedTrace;
    private final Similarity bestSimilarity;

    public ComparisonResult(Trace preprocessedTrace, Similarity bestSimilarity) {
        this.preprocessedTrace = preprocessedTrace;
        this.bestSimilarity = bestSimilarity;
    }

    public Trace getPreprocessedTrace() {
        return preprocessedTrace;
    }

    public Similarity getBestSimilarity() { return bestSimilarity; }
}
