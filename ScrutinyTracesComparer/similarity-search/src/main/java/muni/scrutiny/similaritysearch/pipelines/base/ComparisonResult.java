package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.JFreeChart;

public class ComparisonResult {
    private final Trace preprocessedReferenceTrace;
    private final Trace preprocessedNewTrace;
    private final Similarity bestSimilarity;
    private final JFreeChart chart;

    public ComparisonResult(Trace preprocessedReferenceTrace, Trace preprocessedNewTrace, Similarity bestSimilarity, JFreeChart chart) {
        this.preprocessedReferenceTrace = preprocessedReferenceTrace;
        this.preprocessedNewTrace = preprocessedNewTrace;
        this.bestSimilarity = bestSimilarity;
        this.chart = chart;
    }

    public Trace getPreprocessedReferenceTrace() {
        return preprocessedReferenceTrace;
    }

    public Trace getPreprocessedNewTrace() {
        return preprocessedNewTrace;
    }

    public Similarity getBestSimilarity() { return bestSimilarity; }

    public JFreeChart getChart() { return chart; }
}
