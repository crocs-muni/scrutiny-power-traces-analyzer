package muni.scrutiny.similaritysearch.pipelines.base;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.similaritysearch.measures.base.SimilarityMeasure;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.Layer;

import java.util.ArrayList;
import java.util.List;

public abstract class SlidingWindowTracePipeline extends TracePipeline<ComparisonResult> {
    public int DEFAULT_WINDOW_JUMP = 1;
    protected final int windowJump;
    protected final SimilarityMeasure similarityMeasure;

    public SlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, Preprocessor... preprocessors) {
        super(preprocessors);
        this.windowJump = DEFAULT_WINDOW_JUMP;
        this.similarityMeasure = similarityMeasure;
    }

    public SlidingWindowTracePipeline(SimilarityMeasure similarityMeasure, int windowJump, Preprocessor... preprocessors) {
        super(preprocessors);
        this.windowJump = windowJump;
        this.similarityMeasure = similarityMeasure;
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace newTrace) {
        PreprocessingResult prepResultNewTrace = preprocess(newTrace);
        PreprocessingResult prepResultReferenceTrace = preprocess(referenceTrace);
        Trace newPreprocessedTrace = prepResultNewTrace.getPreprocessedTrace();
        Trace referencePreprocessedTrace = prepResultReferenceTrace.getPreprocessedTrace();
        Trace biggerTrace = referencePreprocessedTrace.getDataCount() >= newPreprocessedTrace.getDataCount()
                ? referencePreprocessedTrace
                : newPreprocessedTrace;
        Trace smallerTrace = referencePreprocessedTrace.getDataCount() < newPreprocessedTrace.getDataCount()
                ? referencePreprocessedTrace
                : newPreprocessedTrace;

        double[] biggerVoltageArray = biggerTrace.getVoltage();
        double[] smallerVoltageArray = smallerTrace.getVoltage();

        Similarity bestSimilarity = findBestSimilarity(biggerVoltageArray, smallerVoltageArray);

        List<ChartTrace> chartTraces = new ArrayList<>();
        if (referencePreprocessedTrace.getDataCount() > newPreprocessedTrace.getDataCount()) {
            chartTraces.add(new ChartTrace(referencePreprocessedTrace, TracePlotter.BLUE, TracePlotter.basicChartStroke));
            chartTraces.add(new ChartTrace(newPreprocessedTrace, TracePlotter.ORANGE, bestSimilarity.getFirstIndex()));
        } else {
            chartTraces.add(new ChartTrace(referencePreprocessedTrace, TracePlotter.BLUE, bestSimilarity.getFirstIndex(), TracePlotter.basicDashedStroke));
            chartTraces.add(new ChartTrace(newPreprocessedTrace, TracePlotter.ORANGE));
        }

        IntervalsDistances distancesIntervals = computeIntervalDistances(biggerVoltageArray, smallerVoltageArray, bestSimilarity);

        JFreeChart jfc = createTintedChart(newPreprocessedTrace, referencePreprocessedTrace, biggerTrace, chartTraces, distancesIntervals);

        return new ComparisonResult(prepResultReferenceTrace.getPreprocessedTrace(), prepResultNewTrace.getPreprocessedTrace(), bestSimilarity, jfc);
    }

    protected JFreeChart createTintedChart(Trace newPreprocessedTrace, Trace referencePreprocessedTrace, Trace biggerTrace, List<ChartTrace> chartTraces, IntervalsDistances distancesIntervals) {
        TracePlotter tp = new TracePlotter(chartTraces);
        String imageName = getName() + "_" + referencePreprocessedTrace.getDisplayName() + "-" + newPreprocessedTrace.getDisplayName();
        JFreeChart jfc = tp.createXYLineChart(imageName, referencePreprocessedTrace.getDisplayTimeUnit(), referencePreprocessedTrace.getDisplayVoltageUnit());
        for (IntervalDistance intervalDistance : distancesIntervals.getIntervalDistances()) {
            double powerCoeff = intervalDistance.getDistance() / distancesIntervals.getMaximalDistance();
            if (powerCoeff > 0.5) {
                double from = biggerTrace.getTimeOnIndex(intervalDistance.getFrom());
                double to = biggerTrace.getTimeOnIndex(intervalDistance.getTo());
                IntervalMarker marker = new IntervalMarker(from, to);
                marker.setPaint(TracePlotter.getColor(powerCoeff));
                marker.setAlpha(0.1f);
                jfc.getXYPlot().addDomainMarker(marker, Layer.BACKGROUND);
            }
        }
        return jfc;
    }

    protected IntervalsDistances computeIntervalDistances(double[] biggerVoltageArray, double[] smallerVoltageArray, Similarity bestSimilarity) {
        int intervalLength = Math.max(1, smallerVoltageArray.length / 100);
        IntervalsDistances distancesIntervals = new IntervalsDistances();
        for (int i = 0; i < smallerVoltageArray.length; i += intervalLength) {
            int firstIndexBiggerVector = bestSimilarity.getFirstIndex() + i;
            int firstIndexSmallerVector = i;
            double dist = similarityMeasure.compute(smallerVoltageArray, biggerVoltageArray, firstIndexSmallerVector, firstIndexBiggerVector, intervalLength);
            distancesIntervals.addDistance(firstIndexBiggerVector, Math.min(firstIndexBiggerVector + intervalLength, biggerVoltageArray.length - 1), dist);
        }

        return distancesIntervals;
    }

    protected Similarity findBestSimilarity(double[] biggerVoltageArray, double[] smallerVoltageArray) {
        int windowStart = 0;
        int windowEnd = smallerVoltageArray.length - 1;
        double bestSimilarity = getInitialSimilarity();
        int windowStartBest = 0;
        int windowEndBest = windowEnd;
        while (windowEnd < biggerVoltageArray.length) {
            double currentWindowDistance = getCurrentWindowDistance(biggerVoltageArray, smallerVoltageArray, windowStart);
            if (isNewMeasureBetter(bestSimilarity, currentWindowDistance)) {
                bestSimilarity = currentWindowDistance;
                windowStartBest = windowStart;
                windowEndBest = windowEnd;
            }

            windowStart += windowJump;
            windowEnd += windowJump;
        }

        return new Similarity(windowStartBest, windowEndBest, bestSimilarity);
    }

    protected double getCurrentWindowDistance(double[] biggerVoltageArray, double[] smallerVoltageArray, int windowStart) {
        double currentWindowDistance = similarityMeasure.compute(smallerVoltageArray, biggerVoltageArray, windowStart);
        return currentWindowDistance;
    }

    protected abstract double getInitialSimilarity();
    protected abstract boolean isNewMeasureBetter(double currentSimilarity, double newSimilarity);

    protected class IntervalsDistances {
        private final List<IntervalDistance> intervalDistances = new ArrayList<>();
        private double maximalDistance = Double.MIN_VALUE;

        public IntervalsDistances() {
        }

        public List<IntervalDistance> getIntervalDistances() {
            return intervalDistances;
        }

        public double getMaximalDistance() {
            return maximalDistance;
        }

        public void addDistance(int from, int to, double distance) {
            if (distance > maximalDistance) {
                maximalDistance = distance;
            }

            intervalDistances.add(new IntervalDistance(from, to, distance));
        }
    }

    protected class IntervalDistance {
        private final int from;
        private final int to;
        private final double distance;

        public IntervalDistance(int from, int to, double distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        public double getDistance() {
            return distance;
        }
    }
}