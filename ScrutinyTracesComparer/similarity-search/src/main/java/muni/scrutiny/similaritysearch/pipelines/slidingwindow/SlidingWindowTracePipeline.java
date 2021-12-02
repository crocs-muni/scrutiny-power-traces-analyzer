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

        double[] biggerVoltageArray = prepResultReferenceTrace.getPreprocessedTrace().getDataCount() >= prepResultNewTrace.getPreprocessedTrace().getDataCount()
                ? prepResultReferenceTrace.getPreprocessedTrace().getVoltage()
                : prepResultNewTrace.getPreprocessedTrace().getVoltage();
        double[] smallerVoltageArray = prepResultReferenceTrace.getPreprocessedTrace().getDataCount() < prepResultNewTrace.getPreprocessedTrace().getDataCount()
                ? prepResultReferenceTrace.getPreprocessedTrace().getVoltage()
                : prepResultNewTrace.getPreprocessedTrace().getVoltage();
        int windowStart = 0;
        int windowEnd = smallerVoltageArray.length - 1;
        Similarity bestSimilarity = new Similarity(windowStart, windowEnd, Double.MAX_VALUE);
        while (windowEnd < biggerVoltageArray.length) {
            double currentDistance = distanceMeasure.compute(smallerVoltageArray, biggerVoltageArray, windowStart);
            if (currentDistance < bestSimilarity.getDistance()) {
                bestSimilarity = new Similarity(windowStart, windowEnd, currentDistance);
            }

            windowStart += windowJump;
            windowEnd += windowJump;
        }

        Trace tr = prepResultReferenceTrace.getPreprocessedTrace();
        Trace tn = prepResultNewTrace.getPreprocessedTrace();
        List<ChartTrace> chartTraces = new ArrayList<>();
        if (tr.getDataCount() > tn.getDataCount()) {
            chartTraces.add(new ChartTrace(tr, TracePlotter.GREEN));
            chartTraces.add(new ChartTrace(tn, TracePlotter.RED, bestSimilarity.getFirstIndex()));
        } else {
            chartTraces.add(new ChartTrace(tr, TracePlotter.GREEN, bestSimilarity.getFirstIndex()));
            chartTraces.add(new ChartTrace(tn, TracePlotter.RED));
        }

        TracePlotter tp = new TracePlotter(chartTraces);
        String imageName = getName() + "_" + tr.getDisplayName() + "-" + tn.getDisplayName();
        JFreeChart jfc = tp.createXYLineChart(imageName, "t" + tr.getTimeUnit(), "U" + tr.getVoltageUnit());

        return new ComparisonResult(prepResultReferenceTrace.getPreprocessedTrace(), prepResultNewTrace.getPreprocessedTrace(), bestSimilarity, jfc);
    }
}
