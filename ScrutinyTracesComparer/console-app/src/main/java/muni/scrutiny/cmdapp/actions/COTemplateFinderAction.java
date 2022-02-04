package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.cotemplate.module.CorrelationComputer;
import muni.cotemplate.module.GPUCorrelationComputer;
import muni.cotemplate.module.configurations.COTemplateConfiguration;
import muni.cotemplate.module.configurations.COTemplateMaskElement;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.Layer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class COTemplateFinderAction extends BaseAction {
    public static final String name = "cotemp";
    private static final String tracePathShort = "-t";
    private static final String configShort = "-c";
    private static final String graphicComputation = "-g";
    private static final String devicesCountShort = "-d";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public COTemplateFinderAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(tracePathShort, new ActionParameter(new ArrayList<String>() {{
                add(tracePathShort);
            }}, true, null));
            put(configShort, new ActionParameter(new ArrayList<String>() {{
                add(configShort);
            }}, true, null));
            put(devicesCountShort, new ActionParameter(new ArrayList<String>() {{
                add(devicesCountShort);
            }}, false, "0"));
        }};
        flags = new HashMap<String, ActionFlag>() {{
            put(graphicComputation, new ActionFlag(new ArrayList<String>() {{
                add(graphicComputation);
            }}, false));
        }};
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
            int devicesCount = getParameterAsInt(devicesCountShort);
            Path tracePath = getParameterAsPath(tracePathShort);
            System.out.println("Trace path:");
            System.out.println(tracePath.toAbsolutePath());
            Trace trace = DataManager.loadTrace(tracePath, false);
            Path configurationPath = getParameterAsPath(configShort);
            System.out.println("Configuration path:");
            System.out.println(configurationPath.toAbsolutePath());
            Path outputPath = configurationPath.getParent();
            COTemplateConfiguration coTemplateConfiguration = getCOTemplateConfig(configurationPath);
            if (!areTimeLengthsEqual(coTemplateConfiguration)) {
                throw new ActionException("Times sizes are not equal.");
            }

            int samplingFrequency = trace.getSamplingFrequency();
            HashMap<Character, Integer> characterCounts = coTemplateConfiguration.getCharacterCounts();
            double[] voltage = trace.getVoltage();
            int timeIterations = coTemplateConfiguration.getimesCount();
            System.out.println("Time iterations: " + timeIterations);
            COTemplateSearchResult coTemplateSearchResult = null;
            long start = System.currentTimeMillis();
            for (int timeIteration = 0; timeIteration < timeIterations; timeIteration++) {
                System.out.println("Starting time iteration: " + (timeIteration+1));
                HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals = new HashMap<>();
                HashMap<Character, Integer> characterWidths = new HashMap<>();
                int lastIntervalIndex = 0;
                lastIntervalIndex = generateMaskIntervalsForTimes(
                        coTemplateConfiguration,
                        samplingFrequency,
                        timeIteration,
                        maskIntervals,
                        characterWidths,
                        lastIntervalIndex);

                if (lastIntervalIndex > voltage.length) {
                    continue;
                }

                int endingIndex = voltage.length - lastIntervalIndex;
                System.out.println("Ending index: " + endingIndex);
                HashMap<Character, double[]> correlations = initializeCorrelations(maskIntervals, endingIndex);
                for (Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals : maskIntervals.entrySet()) {
                    int characterCount = characterCounts.get(characterIntervals.getKey());
                    int segmentWidth = characterWidths.get(characterIntervals.getKey());
                    findMostCorrelatedSegment(voltage, endingIndex, correlations, characterIntervals, characterCount, segmentWidth, devicesCount);
                }

                long end = System.currentTimeMillis();
                System.out.println("Search for most correlated CO ended in: " + (double)(end-start)/1000 + "s");

                Pair<Integer, Double> maxCorrIndexValue = computeWeightedCorrelations(characterCounts, characterWidths, endingIndex, correlations);
                visualizeCOSearch(trace, outputPath, characterCounts, characterWidths, maxCorrIndexValue.getKey(), timeIteration);
                if (coTemplateSearchResult == null || coTemplateSearchResult.correlation < maxCorrIndexValue.getValue()) {
                    coTemplateSearchResult = new COTemplateSearchResult(
                            timeIteration,
                            maskIntervals,
                            characterWidths,
                            lastIntervalIndex,
                            maxCorrIndexValue.getValue(),
                            maxCorrIndexValue.getKey());
                }
            }

            HashMap<Character, double[]> averageTemplates = createAverageTemplates(voltage, coTemplateSearchResult);
            double[] maskTemplate = createMaskTemplate(coTemplateSearchResult, averageTemplates);
            Trace maskTemplateTrace = new Trace(
                    trace.getName(),
                    maskTemplate.length,
                    trace.getVoltageUnit(),
                    trace.getTimeUnit(),
                    maskTemplate,
                    trace.getSamplingFrequency());
            visualizeCOTemplate(maskTemplateTrace, outputPath);
            saveTemplateTrace(trace, outputPath, maskTemplateTrace);
//            for (int windowIndex = 0; windowIndex < voltage.length - maskTemplate.length; windowIndex++) {
//                for (int templateIndex = 0; maskTemplate.length; templateIndex++) {
//
//                }
//            }
        } catch (Exception ex) {
            throw new ActionException(ex);
        }
    }

    private void saveTemplateTrace(Trace trace, Path outputPath, Trace maskTemplateTrace) throws IOException {
        String templateName = trace.getDisplayName() + "-template.csv";
        System.out.println("Template CSV saved to: " + templateName);
        DataManager.saveTrace(outputPath.resolve(templateName), maskTemplateTrace);
    }

    private void visualizeCOTemplate(Trace maskTemplateTrace, Path outputPath) throws IOException {
        TracePlotter tp = new TracePlotter(maskTemplateTrace);
        String chartName = maskTemplateTrace.getDisplayName() + "-Template";
        JFreeChart jfc = tp.createXYLineChart(chartName, maskTemplateTrace.getDisplayTimeUnit(), maskTemplateTrace.getDisplayVoltageUnit());
        String imageName = FileUtils.saveComparisonImage(outputPath, jfc);
        System.out.println("Template image saved to: " + imageName);
    }

    private double[] createMaskTemplate(COTemplateSearchResult coTemplateSearchResult, HashMap<Character, double[]> averageTemplates) {
        double[] maskTemplate = new double[coTemplateSearchResult.wholeTemplateSize];
        for (Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals : coTemplateSearchResult.intervalWidths.entrySet()) {
            double[] characterAverageTemplate = averageTemplates.get(characterIntervals.getKey());
            for (Pair<Integer, Integer> characterInterval : characterIntervals.getValue()) {
                int segmentWidth = 0;
                for (int intervalIndex = characterInterval.getKey(); intervalIndex < characterInterval.getValue(); intervalIndex++) {
                    maskTemplate[intervalIndex] = characterAverageTemplate[segmentWidth];
                    segmentWidth++;
                }
            }
        }
        return maskTemplate;
    }

    private HashMap<Character, double[]> createAverageTemplates(double[] voltage, COTemplateSearchResult coTemplateSearchResult) {
        HashMap<Character, double[]> averageTemplates = new HashMap<>();
        for (Map.Entry<Character, List<Pair<Integer, Integer>>> intervals : coTemplateSearchResult.intervalWidths.entrySet()) {
            double[] characterTemplateAverage = new double[coTemplateSearchResult.characterWidths.get(intervals.getKey())];
            for (Pair<Integer, Integer> interval : intervals.getValue()) {
                int segmentIndex = 0;
                for (int intervalIndex = interval.getKey(); intervalIndex < interval.getValue(); intervalIndex++) {
                    characterTemplateAverage[segmentIndex] += voltage[intervalIndex] / intervals.getValue().size();
                    segmentIndex++;
                }
            }

            averageTemplates.putIfAbsent(intervals.getKey(), characterTemplateAverage);
        }

        return averageTemplates;
    }

    private Pair<Integer, Double> computeWeightedCorrelations(
            HashMap<Character, Integer> characterCounts,
            HashMap<Character, Integer> characterWidths,
            int endingIndex,
            HashMap<Character, double[]> correlations) {
        double maxCorr = Double.MIN_VALUE;
        int maxCorrIndex = 0;
        for (int correlationIndex = 0; correlationIndex < endingIndex; correlationIndex++) {
            double weightedCorrelationsNumerator = 0;
            double weightedCorrelationsDenominator = 0;
            for (Map.Entry<Character, double[]> characterCorrelations : correlations.entrySet()) {
                int characterCount = characterCounts.get(characterCorrelations.getKey());
                int characterWidth = characterWidths.get(characterCorrelations.getKey());
                weightedCorrelationsNumerator += characterCount * characterWidth * characterCorrelations.getValue()[correlationIndex];
                weightedCorrelationsDenominator += characterCount * characterWidth;
            }

            double iterationCorr = weightedCorrelationsNumerator / weightedCorrelationsDenominator;
            if (maxCorr < iterationCorr) {
                maxCorr = iterationCorr;
                maxCorrIndex = correlationIndex;
            }
        }

        System.out.println("Max corr index is: " + maxCorrIndex + " with corr: " + maxCorr);
        return Pair.of(maxCorrIndex, maxCorr);
    }

    private void findMostCorrelatedSegment(
            double[] voltage,
            int endingIndex,
            HashMap<Character, double[]> correlations,
            Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals,
            int characterCount,
            int segmentWidth,
            int devicesCount) throws InterruptedException {
        if (flags.get(graphicComputation).getValueOrDefault()) {
            GPUCorrelationComputer gpucc = new GPUCorrelationComputer(
                    voltage,
                    correlations,
                    characterIntervals,
                    characterCount,
                    segmentWidth,
                    endingIndex,
                    devicesCount);
            gpucc.run();
        } else if (endingIndex > 1000) {
            int cores = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(cores);
            double jump = (double) endingIndex / cores;
            for (int index = 0; index < endingIndex; index += jump) {
                executor.execute(new CorrelationComputer(
                        voltage,
                        correlations,
                        characterIntervals,
                        characterCount,
                        segmentWidth,
                        index,
                        Math.min((int)(index + jump), endingIndex)));
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } else {
            CorrelationComputer correlationComputer = new CorrelationComputer(
                    voltage,
                    correlations,
                    characterIntervals,
                    characterCount,
                    segmentWidth,
                    0,
                    endingIndex);
            correlationComputer.run();
        }
    }

    private void visualizeCOSearch(
            Trace trace,
            Path outputPath,
            HashMap<Character, Integer> characterCounts,
            HashMap<Character, Integer> characterWidths,
            int maxCorrIndex,
            int timeIteration) throws IOException {
        ChartTrace ct = new ChartTrace(trace, TracePlotter.BLUE);
        TracePlotter tp = new TracePlotter(ct);
        String chartName = trace.getDisplayName() + "-" + timeIteration;
        JFreeChart jfc = tp.createXYLineChart(chartName, trace.getDisplayTimeUnit(), trace.getDisplayVoltageUnit());
        IntervalMarker marker = new IntervalMarker(
                trace.getTimeOnIndex(maxCorrIndex),
                trace.getTimeOnIndex(maxCorrIndex + characterWidths.get('X') * characterCounts.get('X')));
        marker.setPaint(TracePlotter.ORANGE);
        marker.setAlpha(0.2f);
        jfc.getXYPlot().addDomainMarker(marker, Layer.BACKGROUND);
        String imageName = FileUtils.saveComparisonImage(outputPath, jfc);
        System.out.println("Image saved to: " + imageName);
    }

    private HashMap<Character, double[]> initializeCorrelations(HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals, int endingIndex) {
        HashMap<Character, double[]> correlations = new HashMap<>();
        for (Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals : maskIntervals.entrySet()) {
            correlations.put(characterIntervals.getKey(), new double[endingIndex]);
        }
        return correlations;
    }

    private int generateMaskIntervalsForTimes(
            COTemplateConfiguration coTemplateConfiguration,
            int samplingFrequency,
            int timeIteration, HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals,
            HashMap<Character, Integer> characterWidths,
            int lastIntervalIndex) {
        for (int maskLetterIndex = 0; maskLetterIndex < coTemplateConfiguration.mask.length(); maskLetterIndex++) {
            Character maskCharacter = coTemplateConfiguration.mask.charAt(maskLetterIndex);
            COTemplateMaskElement maskElement = coTemplateConfiguration.elements.stream().filter(e -> e.maskElement == maskCharacter).findFirst().orElse(new COTemplateMaskElement());
            int dataLengthFromOperationTime = (int)(samplingFrequency * maskElement.times.get(timeIteration));
            characterWidths.putIfAbsent(maskCharacter, dataLengthFromOperationTime);
            if (!maskIntervals.containsKey(coTemplateConfiguration.mask.charAt(timeIteration))) {
                List<Pair<Integer, Integer>> elementIntervals = new ArrayList<>();
                elementIntervals.add(Pair.of(lastIntervalIndex, lastIntervalIndex + dataLengthFromOperationTime));
                maskIntervals.put(maskCharacter, elementIntervals);
            } else {
                maskIntervals.get(maskCharacter).add(Pair.of(lastIntervalIndex, lastIntervalIndex + dataLengthFromOperationTime));
            }

            lastIntervalIndex += dataLengthFromOperationTime;
        }
        return lastIntervalIndex;
    }

    private COTemplateConfiguration getCOTemplateConfig(Path coTemplateConfigurationPath) throws ActionException {
        String coTemplateConfigContent = FileUtils.readFile(coTemplateConfigurationPath);
        return new Gson().fromJson(coTemplateConfigContent, COTemplateConfiguration.class);
    }

    private boolean areTimeLengthsEqual(COTemplateConfiguration coTemplateConfiguration) {
        int len = coTemplateConfiguration.elements.stream().findFirst().orElse(new COTemplateMaskElement()).times.size();
        for (COTemplateMaskElement element : coTemplateConfiguration.elements) {
            if (len != element.times.size()) {
                return false;
            }
        }

        return true;
    }

    class COTemplateSearchResult {
        public int timeIteration;
        public HashMap<Character, List<Pair<Integer, Integer>>> intervalWidths;
        public HashMap<Character, Integer> characterWidths;
        public int wholeTemplateSize;
        public double correlation;
        public int correlationIndex;

        public COTemplateSearchResult(
                int timeIteration,
                HashMap<Character, List<Pair<Integer, Integer>>> intervalWidths,
                HashMap<Character, Integer> characterWidths,
                int wholeTemplateSize,
                double correlation,
                int correlationIndex) {
            this.timeIteration = timeIteration;
            this.intervalWidths = intervalWidths;
            this.characterWidths = characterWidths;
            this.wholeTemplateSize = wholeTemplateSize;
            this.correlation = correlation;
            this.correlationIndex = correlationIndex;
        }
    }
}
