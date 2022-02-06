package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.similaritysearch.measures.base.SimilarityMeasure;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;

public abstract class CorrelationSlidingWindowTracePipeline extends SlidingWindowTracePipeline {
    public CorrelationSlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, Preprocessor... preprocessors) {
        super(similarityMeasure, preprocessors);
    }

    public CorrelationSlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, int windowJump, Preprocessor... preprocessors) {
        super(similarityMeasure, windowJump, preprocessors);
    }

    @Override
    protected double getInitialSimilarity() {
        return Double.MIN_VALUE;
    }

    @Override
    protected boolean isNewMeasureBetter(double currentSimilarity, double newSimilarity) {
        return currentSimilarity < newSimilarity;
    }

    @Override
    protected IntervalsDistances computeIntervalDistances(double[] biggerVoltageArray, double[] smallerVoltageArray, Similarity bestSimilarity) {
        int intervalLength = Math.max(1, smallerVoltageArray.length / 100);
        IntervalsDistances distancesIntervals = new IntervalsDistances();
        for (int i = 0; i < smallerVoltageArray.length; i += intervalLength) {
            int firstIndexBiggerVector = bestSimilarity.getFirstIndex() + i;
            int firstIndexSmallerVector = i;
            double dist = 1 - similarityMeasure.compute(smallerVoltageArray, biggerVoltageArray, firstIndexSmallerVector, firstIndexBiggerVector, intervalLength);
            distancesIntervals.addDistance(firstIndexBiggerVector, Math.min(firstIndexBiggerVector + intervalLength, biggerVoltageArray.length - 1), dist);
        }

        return distancesIntervals;
    }
}
