package muni.scrutiny.module.pipelines.base;

import com.dtw.WarpPath;
import com.matrix.ColMajorCell;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.similaritysearch.measures.dynamictimewarping.DynamicTimeWarpingDistance;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.similaritysearch.pipelines.base.Similarity;
import muni.scrutiny.similaritysearch.pipelines.base.TracePipeline;
import muni.scrutiny.similaritysearch.preprocessing.filtering.ButterworthLowpassFilter;
import muni.scrutiny.similaritysearch.preprocessing.offsetting.SimpleOffsetNormalizer;
import muni.scrutiny.similaritysearch.preprocessing.resampling.TraceIntervalResampler;
import muni.scrutiny.similaritysearch.preprocessing.resampling.intervalprocessor.MeanProcessor;
import muni.scrutiny.similaritysearch.preprocessing.rescaling.SimpleRescaler;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.List;

public class PreprocessedDynamicWarpingPipeline extends TracePipeline<ComparisonResult> {
    public static final String name = "pdtwp";

    public PreprocessedDynamicWarpingPipeline(
            int desiredSamplingFrequency,
            double referenceMinimum,
            double referenceMaximum,
            CustomPipelineParameters customParameters) {
        super(new TraceIntervalResampler(desiredSamplingFrequency, new MeanProcessor(), 1),
                new ButterworthLowpassFilter(customParameters == null ? null :customParameters.getDoubleParameter("cutoffFrequency")),
                new SimpleOffsetNormalizer(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("offset"), customParameters == null ? null :customParameters.getDoubleParameter("normalizerInvervalCoefficient")),
                new SimpleRescaler(referenceMinimum, referenceMaximum, customParameters == null ? null : customParameters.getDoubleParameter("scale"), customParameters == null ? null : customParameters.getDoubleParameter("scalerInvervalCoefficient")));
    }

    @Override
    public ComparisonResult compare(Trace referenceTrace, Trace preprocessedTrace) {
        PreprocessingResult prepResultReferenceTrace = preprocess(referenceTrace);
        PreprocessingResult prepResultNewTrace = preprocess(preprocessedTrace);
        double[] prepRefVoltage = prepResultReferenceTrace.getPreprocessedTrace().getVoltage();
        double[] prepNewVoltage = prepResultNewTrace.getPreprocessedTrace().getVoltage();
        double[] prepNewVoltageScaled = new double[prepNewVoltage.length];
        for (int i = 0; i < prepNewVoltage.length; i++) {
            prepNewVoltageScaled[i] = 2*prepResultReferenceTrace.getPreprocessedTrace().getMaximalVoltage() + prepNewVoltage[i];
        }

        double[] prepRefTime = prepResultReferenceTrace.getPreprocessedTrace().getTime(false);
        double[] prepNewTime = prepResultNewTrace.getPreprocessedTrace().getTime(false);
        DynamicTimeWarpingDistance dtwd = new DynamicTimeWarpingDistance();
        double dist = dtwd.compute(
                prepRefVoltage,
                prepNewVoltage,
                0);
        WarpPath warpingPath = new DynamicTimeWarpingDistance().getWarpingPath(
                prepRefVoltage,
                prepNewVoltage);
        Trace tr = prepResultReferenceTrace.getPreprocessedTrace();
        Trace tn = prepResultNewTrace.getPreprocessedTrace();
        Trace tnScaled = new Trace(tn.getName(), tn.getDataCount(), tn.getVoltageUnit(), tn.getTimeUnit(), prepNewVoltageScaled, tn.getSamplingFrequency());
        List<ChartTrace> chartTraces = new ArrayList<>();
        if (tr.getDataCount() > tn.getDataCount()) {
            chartTraces.add(new ChartTrace(tr, TracePlotter.BLUE));
            chartTraces.add(new ChartTrace(tnScaled, TracePlotter.YELLOW, 0));
        } else {
            chartTraces.add(new ChartTrace(tr, TracePlotter.BLUE, 0));
            chartTraces.add(new ChartTrace(tnScaled, TracePlotter.YELLOW));
        }

        int jumpSize = warpingPath.size() > 10000 ? (int)Math.ceil((double)warpingPath.size() / 100) : 1;
        List<XYSeries> additionalSeries = new ArrayList<>();
        for (int i = 0; i < warpingPath.size(); i = i + jumpSize) {
            XYSeries series = new XYSeries("path" + i);
            ColMajorCell cmc = warpingPath.get(i);
            series.add(prepRefTime[cmc.getCol()], prepRefVoltage[cmc.getCol()]);
            series.add(prepNewTime[cmc.getRow()], prepNewVoltageScaled[cmc.getRow()]);
            additionalSeries.add(series);
        }

        TracePlotter tp = new TracePlotter(chartTraces, additionalSeries);
        String imageName = name + "_" + tr.getDisplayName() + "-" + tn.getDisplayName();
        JFreeChart jfc = tp.createXYLineChart(imageName, "t" + tr.getTimeUnit(), "U" + tr.getVoltageUnit());
        return new ComparisonResult(referenceTrace, preprocessedTrace, new Similarity(0, 0, dist), jfc);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMetricType() {
        return "distance";
    }
}
