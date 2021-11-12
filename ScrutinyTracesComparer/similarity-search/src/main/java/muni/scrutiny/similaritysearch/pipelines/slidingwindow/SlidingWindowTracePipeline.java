package muni.scrutiny.similaritysearch.pipelines.slidingwindow;

import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.Similarity;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;

public class SlidingWindowTracePipeline extends TracePipeline<ComparisonResult> {
    public int DEFAULT_WINDOW_JUMP = 1;
    private final int windowJump;
    private final DistanceMeasure distanceMeasure;

    public SlidingWindowTracePipeline(DistanceMeasure distanceMeasure, Preprocessor... preprocessors) {
        super(preprocessors);
        this.windowJump = DEFAULT_WINDOW_JUMP;
        this.distanceMeasure = distanceMeasure;
    }

    public SlidingWindowTracePipeline(DistanceMeasure distanceMeasure, int windowJump, Preprocessor... preprocessors) {
        super(preprocessors);
        this.windowJump = windowJump;
        this.distanceMeasure = distanceMeasure;
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace newTrace) {
        if (!isTracePreprocessed && preprocessedTrace != null) {
            preprocess(newTrace);
        }

        double[] biggerVoltageArray = referenceTrace.getDataCount() >= preprocessedTrace.getDataCount()
                ? referenceTrace.getVoltage()
                : preprocessedTrace.getVoltage();
        double[] smallerVoltageArray = referenceTrace.getDataCount() < preprocessedTrace.getDataCount()
                ? referenceTrace.getVoltage()
                : preprocessedTrace.getVoltage();
        int windowStart = 0;
        int windowEnd = smallerVoltageArray.length - 1;
        Similarity bestSimilarity = new Similarity(windowStart, windowEnd, Double.MAX_VALUE);
        while (windowEnd < biggerVoltageArray.length) {
            double currentDistance = distanceMeasure.compute(biggerVoltageArray, smallerVoltageArray, windowStart);
            if (currentDistance < bestSimilarity.getDistance()) {
                bestSimilarity = new Similarity(windowStart, windowEnd, currentDistance);
            }

            windowEnd += windowJump;
            windowStart += windowJump;
        }

        return new ComparisonResult(preprocessedTrace, bestSimilarity);
    }
}
