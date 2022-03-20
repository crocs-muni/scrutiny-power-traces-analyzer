package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.cotemplate.module.CorrelationComputer;
import muni.cotemplate.module.GPUCorrelationComputer;
import muni.cotemplate.module.configurations.input.COTemplateConfiguration;
import muni.cotemplate.module.configurations.input.COTemplateMaskElement;
import muni.cotemplate.module.configurations.output.COTemplateFinderResult;
import muni.cotemplate.module.configurations.output.COTemplateFinderWidthResult;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.module.configurations.module.TracesComparerDevice;
import muni.scrutiny.module.configurations.module.TracesComparerModule;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.helpers.UnitsHelper;
import muni.scrutiny.traces.models.Trace;
import org.apache.commons.lang3.tuple.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.ui.Layer;

import java.io.IOException;
import java.io.PrintWriter;
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
            COTemplateFinderResult cotfr = new COTemplateFinderResult();
            cotfr.usedConfig = coTemplateConfiguration;
            cotfr.realTracePath = tracePath.getFileName().toString();
            cotfr.partialResults = new ArrayList<>();
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
                    findMostCorrelatedSegment(voltage, trace.getMaximalVoltage(), endingIndex, correlations, characterIntervals, characterCount, segmentWidth, devicesCount);
                }

                long end = System.currentTimeMillis();
                System.out.println("Search for most correlated CO ended in: " + (double)(end-start)/1000 + "s");

                WeightedCorrelationResult wcr = computeWeightedCorrelations(characterCounts, characterWidths, voltage.length, correlations);
                visualizeCOSearch(trace, outputPath, wcr, timeIteration, lastIntervalIndex, cotfr);
                if (coTemplateSearchResult == null || coTemplateSearchResult.correlation < wcr.maxCorrelationValue) {
                    coTemplateSearchResult = new COTemplateSearchResult(
                            timeIteration,
                            maskIntervals,
                            characterWidths,
                            lastIntervalIndex,
                            wcr.maxCorrelationValue,
                            wcr.maxCorrelationIndex);
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
            visualizeCOTemplate(maskTemplateTrace, outputPath, cotfr);
            cotfr.operationTemplatePath = saveTemplateTrace(trace, outputPath, maskTemplateTrace);

            int templateFloatingWindowIterations = voltage.length - maskTemplate.length;
            double[] templateCorrelations = new double[voltage.length];
            for (int windowIndex = 0; windowIndex < templateFloatingWindowIterations; windowIndex++) {
                templateCorrelations[windowIndex] = CorrelationComputer.correlationCoefficientStable(
                    voltage,
                    maskTemplate,
                    windowIndex,
                    windowIndex + maskTemplate.length,
                    maskTemplate.length
                );
            }

            Trace correlationTrace = new Trace(
                    "Correlations_whole_COtemplate.csv",
                    trace.getDataCount(),
                    trace.getVoltageUnit(),
                    trace.getTimeUnit(),
                    templateCorrelations,
                    trace.getSamplingFrequency());
            cotfr.wholeOperationCorrelationPath = saveTrace(correlationTrace, outputPath);
            visualizeCOTemplate(correlationTrace, outputPath, cotfr);
            cotfr.operationLength = maskTemplate.length;
            cotfr.operationLengthTime = UnitsHelper.convertToSeconds(maskTemplateTrace.getExecutionTime(), maskTemplateTrace.getTimeUnit());
            try (PrintWriter out = new PrintWriter(outputPath.resolve("cotemplatefinder_result.json").toFile())) {
                out.println(new Gson().toJson(cotfr));
            }
        } catch (Exception ex) {
            throw new ActionException(ex);
        }
    }

    private String saveTemplateTrace(Trace trace, Path outputPath, Trace maskTemplateTrace) throws IOException {
        String templateName = "Template_" + trace.getDisplayName() + ".csv";
        System.out.println("Template CSV saved to: " + templateName);
        DataManager.saveTrace(outputPath.resolve(templateName), maskTemplateTrace);
        return templateName;
    }

    private String saveTrace(Trace trace, Path outputPath) throws IOException {
        String templateName = trace.getName();
        System.out.println("Correlations CSV saved to: " + templateName);
        DataManager.saveTrace(outputPath.resolve(templateName), trace);
        return templateName;
    }

    private void visualizeCOTemplate(Trace maskTemplateTrace, Path outputPath, COTemplateFinderResult cotfr) throws IOException {
        TracePlotter tp = new TracePlotter(maskTemplateTrace);
        String chartName = "Template_" + maskTemplateTrace.getDisplayName();
        JFreeChart jfc = tp.createXYLineChart(chartName, maskTemplateTrace.getDisplayTimeUnit(), maskTemplateTrace.getDisplayVoltageUnit());
        String imageName = FileUtils.saveComparisonImage(outputPath, jfc);
        System.out.println("Template image saved to: " + imageName);
        cotfr.templateImagePath = imageName;
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
                    characterTemplateAverage[segmentIndex] += voltage[coTemplateSearchResult.correlationIndex + intervalIndex] / intervals.getValue().size();
                    segmentIndex++;
                }
            }

            averageTemplates.putIfAbsent(intervals.getKey(), characterTemplateAverage);
        }

        return averageTemplates;
    }

    private WeightedCorrelationResult computeWeightedCorrelations(
            HashMap<Character, Integer> characterCounts,
            HashMap<Character, Integer> characterWidths,
            int voltageLength,
            HashMap<Character, double[]> correlations) {
        WeightedCorrelationResult wcr = new WeightedCorrelationResult(0, Double.MIN_VALUE, new double[voltageLength]);
        for (int correlationIndex = 0; correlationIndex < voltageLength; correlationIndex++) {
            double weightedCorrelationsNumerator = 0;
            double weightedCorrelationsDenominator = 0;
            for (Map.Entry<Character, double[]> characterCorrelations : correlations.entrySet()) {
                if (correlationIndex >= characterCorrelations.getValue().length) {
                    weightedCorrelationsNumerator = 0;
                    weightedCorrelationsDenominator = 1;
                    break;
                }

                int characterCount = characterCounts.get(characterCorrelations.getKey());
                int characterWidth = characterWidths.get(characterCorrelations.getKey());
                weightedCorrelationsNumerator += characterCount * characterWidth * characterCorrelations.getValue()[correlationIndex];
                weightedCorrelationsDenominator += characterCount * characterWidth;
            }

            double iterationCorr = weightedCorrelationsNumerator / weightedCorrelationsDenominator;
            wcr.correlationsArray[correlationIndex] = iterationCorr;
            if (wcr.maxCorrelationValue < iterationCorr) {
                wcr.maxCorrelationValue = iterationCorr;
                wcr.maxCorrelationIndex = correlationIndex;
            }
        }

        System.out.println("Max corr index is: " + wcr.maxCorrelationIndex + " with corr: " + wcr.maxCorrelationValue);
        return wcr;
    }

    private void findMostCorrelatedSegment(
            double[] voltage,
            double voltageMaximum,
            int endingIndex,
            HashMap<Character, double[]> correlations,
            Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals,
            int characterCount,
            int segmentWidth,
            int devicesCount) throws InterruptedException {
        if (flags.get(graphicComputation).getValueOrDefault()) {
            GPUCorrelationComputer gpucc = new GPUCorrelationComputer(
                    voltage,
                    voltageMaximum,
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
            WeightedCorrelationResult wcr,
            int timeIteration,
            int lastIndex,
            COTemplateFinderResult cotfr) throws IOException {
        COTemplateFinderWidthResult cotfwr = new COTemplateFinderWidthResult();
        ChartTrace ct = new ChartTrace(trace, TracePlotter.BLUE);
        TracePlotter tp = new TracePlotter(ct);
        String chartName = "Highlighted_" + trace.getDisplayName() + "-" + timeIteration;
        JFreeChart jfc = tp.createXYLineChart(chartName, trace.getDisplayTimeUnit(), trace.getDisplayVoltageUnit());
        IntervalMarker marker = new IntervalMarker(
                trace.getTimeOnIndex(wcr.maxCorrelationIndex),
                trace.getTimeOnIndex(wcr.maxCorrelationIndex + lastIndex));
        marker.setPaint(TracePlotter.ORANGE);
        marker.setAlpha(0.2f);
        jfc.getXYPlot().addDomainMarker(marker, Layer.BACKGROUND);
        cotfwr.highlightedTraceImagePath = FileUtils.saveComparisonImage(outputPath, jfc);
        System.out.println("Highlighted area image saved to: " + cotfwr.highlightedTraceImagePath);

        tp = new TracePlotter(wcr.getCorrelationsChartTrace(trace));
        chartName = "Correlations-" + trace.getDisplayName() + "-" + timeIteration;
        jfc = tp.createXYLineChart(chartName, trace.getDisplayTimeUnit(), trace.getDisplayVoltageUnit());
        cotfwr.suboperationsCorrelationImagePath = FileUtils.saveComparisonImage(outputPath, jfc);
        System.out.println("Correlations image saved to: " + cotfwr.suboperationsCorrelationImagePath);
        cotfr.partialResults.add(cotfwr);
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
            int timeIteration,
            HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals,
            HashMap<Character, Integer> characterWidths,
            int lastIntervalIndex) {
        for (int maskLetterIndex = 0; maskLetterIndex < coTemplateConfiguration.mask.length(); maskLetterIndex++) {
            Character maskCharacter = coTemplateConfiguration.mask.charAt(maskLetterIndex);
            COTemplateMaskElement maskElement = coTemplateConfiguration.elements.stream().filter(e -> e.maskElement == maskCharacter).findFirst().orElse(new COTemplateMaskElement());
            int dataLengthFromOperationTime = (int)(samplingFrequency * maskElement.times.get(timeIteration));
            characterWidths.putIfAbsent(maskCharacter, dataLengthFromOperationTime);
            if (!maskIntervals.containsKey(maskCharacter)) {
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

    static class COTemplateSearchResult {
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

    static class WeightedCorrelationResult {
        public int maxCorrelationIndex;
        public double maxCorrelationValue;
        public double[] correlationsArray;

        public WeightedCorrelationResult(
                int maxCorrelationIndex,
                double maxCorrelationValue,
                double[] correlationsArray) {
            this.maxCorrelationIndex = maxCorrelationIndex;
            this.maxCorrelationValue = maxCorrelationValue;
            this.correlationsArray = correlationsArray;
        }

        public ChartTrace getCorrelationsChartTrace(Trace traceCorrelationsBasedOn) {
            Trace correlationTrace = new Trace(
                    "Correlations",
                    traceCorrelationsBasedOn.getDataCount(),
                    traceCorrelationsBasedOn.getVoltageUnit(),
                    traceCorrelationsBasedOn.getTimeUnit(),
                    correlationsArray,
                    traceCorrelationsBasedOn.getSamplingFrequency());
            return new ChartTrace(correlationTrace, TracePlotter.BLUE);
        }
    }
}
