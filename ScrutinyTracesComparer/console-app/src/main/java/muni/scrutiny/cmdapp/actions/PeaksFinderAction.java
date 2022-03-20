package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.cotemplate.module.configurations.output.COTemplateFinderResult;
import muni.cotemplate.module.configurations.output.COTemplatePeaksResult;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.similaritysearch.collections.SimilaritySet;
import muni.scrutiny.similaritysearch.collections.SimilaritySetType;
import muni.scrutiny.similaritysearch.pipelines.base.Similarity;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.IntervalMarker;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PeaksFinderAction extends BaseAction {
    public static final String name = "peaks";
    private static final String configPathShort = "-c";
    private static final String desiredNShort = "-n";
    private static final String pShort = "-p";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public PeaksFinderAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(configPathShort, new ActionParameter(new ArrayList<String>() {{
                add(configPathShort);
            }}, true, null));
            put(desiredNShort, new ActionParameter(new ArrayList<String>() {{
                add(desiredNShort);
            }}, true, null));
            put(pShort, new ActionParameter(new ArrayList<String>() {{
                add(pShort);
            }}, false, "0.99"));
        }};
        flags = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, ActionParameter> getActionParameters() {
        return parameters;
    }

    @Override
    public Map<String, ActionFlag> getActionFlags() {
        return flags;
    }

    @Override
    public void executeAction(String[] arguments) throws ActionException {
        try {
            super.executeAction(arguments);
            Path configPath = getParameterAsPath(configPathShort);
            COTemplateFinderResult cotfr = getCOTemplateFinderResult(configPath);
            Path outputPath = configPath.getParent();
            Trace correlationsTrace = DataManager.loadTrace(outputPath.resolve(cotfr.wholeOperationCorrelationPath), false);
            Trace realTrace = DataManager.loadTrace(outputPath.resolve(cotfr.realTracePath), true);
            double[] traceVoltage = correlationsTrace.getVoltage();
            int width = cotfr.operationLength;
            int desiredN = getParameterAsInt(desiredNShort);
            int topNOccurences = traceVoltage.length / width;
            if (desiredN > topNOccurences) {
                throw new ActionException("Cannot find " + desiredN + " operations because according to the width there can be only " + topNOccurences);
            }

            SimilaritySet simSet = getSimilaritiesSortedSet(traceVoltage, width, topNOccurences);
            double[] topNValues = getTopNValues(desiredN, simSet);
            double ciLowerBound = getConfidenceIntervalLowerBound(desiredN, topNValues);
            SortedSet<Similarity> similaritiesInInterval = new TreeSet<>();
            List<Double> startingTimes = new ArrayList<>();
            List<XYTextAnnotation> annotations = new ArrayList<>();
            List<IntervalMarker> markers = new ArrayList<>();
            int n = getConfidenceIntervalMembers(realTrace, simSet, ciLowerBound, similaritiesInInterval, startingTimes, annotations, markers);

            TracePlotter tp = new TracePlotter(realTrace);
            JFreeChart jfc = tp.createXYLineChart(realTrace.getDisplayName(), realTrace.getDisplayTimeUnit(), realTrace.getDisplayVoltageUnit());
            for (int i = 0; i < annotations.size(); i++) {
                jfc.getXYPlot().addAnnotation(annotations.get(i));
                jfc.getXYPlot().addDomainMarker(markers.get(i));
            }

            COTemplatePeaksResult cotpr = new COTemplatePeaksResult();
            cotpr.expectedN = desiredN;
            cotpr.realN = n;
            cotpr.isMatch = n == desiredN;
            cotpr.startingTimes = startingTimes;
            cotpr.nVisualizationImagePath = FileUtils.saveComparisonImage(outputPath, jfc);
            cotpr.confidenceCoefficient = getParameterAsDouble(pShort);
            cotpr.allCandidates = simSet.getSimilarities().stream().map(x -> x.getDistance()).collect(Collectors.toList());
            cotpr.chosenCandidates = Arrays.stream(topNValues).boxed().collect(Collectors.toList());
            cotpr.confidenceIntervalLowerBound = ciLowerBound;

            try (PrintWriter out = new PrintWriter(outputPath.resolve("peaks_result.json").toFile())) {
                out.println(new Gson().toJson(cotpr));
            }
        } catch (Exception ex) {
            throw new ActionException(ex);
        }
    }

    private int getConfidenceIntervalMembers(
            Trace realTrace,
            SimilaritySet simSet,
            double ciLowerBound,
            SortedSet<Similarity> similaritiesInInterval,
            List<Double> startingTimes,
            List<XYTextAnnotation> annotations,
            List<IntervalMarker> markers) {
        int n = 0;
        for (Similarity s : simSet.getSimilarities()) {
            if (s.getDistance() >= ciLowerBound) {
                similaritiesInInterval.add(s);
                double timeFrom = realTrace.getTimeOnIndex(s.getFirstIndex());
                double timeTo = realTrace.getTimeOnIndex(s.getLastIndex());
                startingTimes.add(timeFrom);
                XYTextAnnotation annot = new XYTextAnnotation(
                        new DecimalFormat("#.0000").format(s.getDistance()),
                        (timeFrom + timeTo)/2,
                        realTrace.getMaximalVoltage());
                annot.setFont(TracePlotter.f1BoldFont);
                annotations.add(annot);
                IntervalMarker im = new IntervalMarker(timeFrom, timeTo);
                im.setPaint(TracePlotter.ORANGE);
                im.setAlpha(0.2f);
                markers.add(im);
                n++;
            }
        }

        return n;
    }

    private double[] getTopNValues(int desiredN, SimilaritySet simSet) {
        SortedSet<Similarity> simSetTemp = new TreeSet<>(simSet.getSimilarities());
        double[] topNValues = new double[desiredN];
        for (int i = 0; i < desiredN; i++) {
            Similarity s = simSetTemp.last();
            simSetTemp.remove(s);
            topNValues[i] = s.getDistance();
        }
        return topNValues;
    }

    private SimilaritySet getSimilaritiesSortedSet(double[] traceVoltage, int width, int topNOccurences) {
        SimilaritySet simSet = new SimilaritySet(topNOccurences, SimilaritySetType.CORRELATION);
        for (int i = 0; i < traceVoltage.length - width; i++) {
            Similarity s = new Similarity(i, i+ width, traceVoltage[i]);
            simSet.add(s);
        }
        return simSet;
    }

    private double getConfidenceIntervalLowerBound(int desiredN, double[] topNValues) {
        TDistribution tdist = new TDistribution(desiredN - 1);
        double prob = tdist.inverseCumulativeProbability(getParameterAsDouble(pShort));
        double sigma = getSigma(topNValues);
        double mean = StatUtils.mean(topNValues);
        return mean - (sigma/Math.sqrt(desiredN))*prob;
    }

    private double getSigma(double[] data) {
        return Math.sqrt(StatUtils.variance(data));
    }

    private COTemplateFinderResult getCOTemplateFinderResult(Path coTemplateFinderResultPath) throws ActionException {
        String coTemplateFinderResultContent = FileUtils.readFile(coTemplateFinderResultPath);
        return new Gson().fromJson(coTemplateFinderResultContent, COTemplateFinderResult.class);
    }
}
