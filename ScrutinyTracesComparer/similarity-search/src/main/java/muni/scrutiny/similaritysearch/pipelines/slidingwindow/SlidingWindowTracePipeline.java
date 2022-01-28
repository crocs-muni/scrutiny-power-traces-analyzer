package muni.scrutiny.similaritysearch.pipelines.slidingwindow;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.similaritysearch.measures.base.DistanceMeasure;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.similaritysearch.pipelines.base.Similarity;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.Layer;

import java.util.ArrayList;
import java.util.List;

public abstract class SlidingWindowTracePipeline extends TracePipeline<ComparisonResult> {
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

        IntervalsDistances distancesIntervals = computeIntervalDistances(biggerVoltageArray, smallerVoltageArray, bestSimilarity);

        List<ChartTrace> chartTraces = new ArrayList<>();
        if (referencePreprocessedTrace.getDataCount() > newPreprocessedTrace.getDataCount()) {
            chartTraces.add(new ChartTrace(referencePreprocessedTrace, TracePlotter.BLUE, TracePlotter.basicChartStroke));
            chartTraces.add(new ChartTrace(newPreprocessedTrace, TracePlotter.ORANGE, bestSimilarity.getFirstIndex()));
        } else {
            chartTraces.add(new ChartTrace(referencePreprocessedTrace, TracePlotter.BLUE, bestSimilarity.getFirstIndex(), TracePlotter.basicDashedStroke));
            chartTraces.add(new ChartTrace(newPreprocessedTrace, TracePlotter.ORANGE));
        }

        TracePlotter tp = new TracePlotter(chartTraces);
        String imageName = getName() + "_" + referencePreprocessedTrace.getDisplayName() + "-" + newPreprocessedTrace.getDisplayName();
        JFreeChart jfc = tp.createXYLineChart(imageName, referencePreprocessedTrace.getDisplayTimeUnit(), referencePreprocessedTrace.getDisplayVoltageUnit());
        for (IntervalDistance intervalDistance : distancesIntervals.getIntervalDistances()) {
            double from = biggerTrace.getTimeOnIndex(intervalDistance.getFrom());
            double to = biggerTrace.getTimeOnIndex(intervalDistance.getTo());
            double colorPowerCoefficient = intervalDistance.getDistance()/distancesIntervals.getMaximalDistance();
            IntervalMarker marker = new IntervalMarker(from, to);
            marker.setPaint(TracePlotter.getColor(intervalDistance.getDistance() / distancesIntervals.getMaximalDistance()));
            marker.setAlpha(0.1f);
            jfc.getXYPlot().addDomainMarker(marker, Layer.BACKGROUND);
        }

        return new ComparisonResult(prepResultReferenceTrace.getPreprocessedTrace(), prepResultNewTrace.getPreprocessedTrace(), bestSimilarity, jfc);
    }

    private IntervalsDistances computeIntervalDistances(double[] biggerVoltageArray, double[] smallerVoltageArray, Similarity bestSimilarity) {
        int intervalLength = Math.max(1, smallerVoltageArray.length / 100);
        IntervalsDistances distancesIntervals = new IntervalsDistances();
        for (int i = 0; i < smallerVoltageArray.length; i += intervalLength) {
            int firstIndexBiggerVector = bestSimilarity.getFirstIndex() + i;
            int firstIndexSmallerVector = i;
            double dist = distanceMeasure.compute(smallerVoltageArray, biggerVoltageArray, firstIndexSmallerVector, firstIndexBiggerVector, intervalLength);
            distancesIntervals.addDistance(firstIndexBiggerVector, Math.min(firstIndexBiggerVector + intervalLength, biggerVoltageArray.length - 1), dist);
        }

        return distancesIntervals;
    }

    private Similarity findBestSimilarity(double[] biggerVoltageArray, double[] smallerVoltageArray) {
        int windowStart = 0;
        int windowEnd = smallerVoltageArray.length - 1;
        Similarity bestSimilarity = new Similarity(windowStart, windowEnd, distanceMeasure.getWorstSimilarity());
        while (windowEnd < biggerVoltageArray.length) {
            double currentDistance = distanceMeasure.compute(smallerVoltageArray, biggerVoltageArray, windowStart);
            if (distanceMeasure.isBetterSimilarity(bestSimilarity.getDistance(), currentDistance)) {
                bestSimilarity = new Similarity(windowStart, windowEnd, currentDistance);
            }

            windowStart += windowJump;
            windowEnd += windowJump;
        }

        return bestSimilarity;
    }
}

class IntervalsDistances {
    private List<IntervalDistance> intervalDistances = new ArrayList<>();
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

class IntervalDistance {
    private int from;
    private int to;
    private double distance;

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