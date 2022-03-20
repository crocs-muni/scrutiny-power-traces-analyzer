package muni.scrutiny.similaritysearch.collections;

import muni.scrutiny.similaritysearch.pipelines.base.Similarity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.SortedSet;
import java.util.TreeSet;

public class SimilaritySet {
    private final SortedSet<Similarity> similarities;
    private final int maxCount;
    private final SimilaritySetType type;
    private final double lowerBound;
    private final double upperBound;

    public SimilaritySet(SimilaritySetType type) {
        this.similarities = new TreeSet<>();
        this.maxCount = Integer.MAX_VALUE;
        this.type = type;
        this.lowerBound = Double.MIN_VALUE;
        this.upperBound = Double.MAX_VALUE;
    }

    public SimilaritySet(int maxCount, SimilaritySetType type) {
        similarities = new TreeSet<>();
        this.maxCount = maxCount;
        this.type = type;
        this.lowerBound = Double.MIN_VALUE;
        this.upperBound = Double.MAX_VALUE;
    }

    public SimilaritySet(SimilaritySetType type, double lowerBound, double upperBound) {
        similarities = new TreeSet<>();
        this.maxCount = Integer.MAX_VALUE;
        this.type = type;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public SimilaritySet(int maxCount, SimilaritySetType type, double lowerBound, double upperBound) {
        similarities = new TreeSet<>();
        this.maxCount = maxCount;
        this.type = type;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public SortedSet<Similarity> getSimilarities() {
        return similarities;
    }

    public void add(Similarity similarity) {
        if (!(similarity.getDistance() >= lowerBound && similarity.getDistance() <= upperBound)) {
            return;
        }

        if (similarities.isEmpty()) {
            similarities.add(similarity);
            return;
        }

        if (compareValues(similarity.getDistance(), similarities.first().getDistance()) || !isFull())
        {
            Pair<Boolean, Similarity> overlapping = isOverlapping(similarity);
            if (overlapping.getKey() && compareValues(similarity.getDistance(), overlapping.getValue().getDistance())) {
                similarities.remove(overlapping.getValue());
                similarities.add(similarity);
                return;
            }
            if (!overlapping.getKey() && isFull()) {
                similarities.remove(similarities.first());
                similarities.add(similarity);
                return;
            }
            if (!overlapping.getKey() && !isFull()) {
                similarities.add(similarity);
            }
        }
    }

    public void addRange(double[] data, int executionTimeIndexes, double samplingCoeff) {
        for (int i = 0; i < data.length; i++) {
            this.add(new Similarity((int)(i*samplingCoeff), (int)((i + executionTimeIndexes)*samplingCoeff), data[i]));
        }
    }

    private boolean isFull() {
        return similarities.size() >= maxCount;
    }

    private Pair<Boolean, Similarity> isOverlapping(Similarity similarityToAdd) {
        for (Similarity similarity : similarities) {
            if (similarity.getLastIndex() > similarityToAdd.getFirstIndex() && similarity.getFirstIndex() < similarityToAdd.getLastIndex())
                return Pair.of(true, similarity);
        }
        return Pair.of(false, null);
    }

    private boolean compareValues(double newValue, double oldValue) {
        if (type == SimilaritySetType.CORRELATION) {
            return newValue > oldValue;
        }

        if (type == SimilaritySetType.DISTANCE) {
            return newValue < oldValue;
        }

        return false;
    }
}
