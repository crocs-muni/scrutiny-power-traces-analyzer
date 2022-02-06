package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.similaritysearch.measures.base.SimilarityMeasure;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;

public abstract class DistanceSlidingWindowTracePipeline extends SlidingWindowTracePipeline {
    public DistanceSlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, Preprocessor... preprocessors) {
        super(similarityMeasure, preprocessors);
    }

    public DistanceSlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, int windowJump, Preprocessor... preprocessors) {
        super(similarityMeasure, windowJump, preprocessors);
    }

    @Override
    protected double getInitialSimilarity() {
        return Double.MAX_VALUE;
    }

    @Override
    protected boolean isNewMeasureBetter(double currentSimilarity, double newSimilarity) {
        return currentSimilarity > newSimilarity;
    }
}
