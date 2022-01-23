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
            long start = System.currentTimeMillis();
            for (int timeIteration = 0; timeIteration < timeIterations; timeIteration++) { // For each different time
                System.out.println("Starting time iteration: " + timeIteration);
                HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals = new HashMap<>(); // Create dictionary letter -> intervals
                HashMap<Character, Integer> characterWidths = new HashMap<>();
                int lastIntervalIndex = 0; // Last index from iteration
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
                    if (flags.get(graphicComputation).getValueOrDefault()) {
                        GPUCorrelationComputer gpucc = new GPUCorrelationComputer(
                                voltage,
                                correlations,
                                characterIntervals,
                                characterCount,
                                segmentWidth,
                                endingIndex);
                        gpucc.run();
                    } else if (endingIndex > 1000) {
                        int cores = Runtime.getRuntime().availableProcessors();
                        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(cores);
                        double jump = (double)endingIndex / cores;
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

                long end = System.currentTimeMillis();
                System.out.println("Computation ended in: " + (double)(end-start)/1000 + "s");
                double maxCorr = Double.MIN_VALUE;
                int maxCorrIndex = 0;
                double[] corrs = correlations.get('X');
                for (int i = 0; i < corrs.length; i++) {
                    if (maxCorr < corrs[i]) {
                        maxCorr = corrs[i];
                        maxCorrIndex = i;
                    }
                }

                ChartTrace ct = new ChartTrace(trace, TracePlotter.BLUE);
                TracePlotter tp = new TracePlotter(ct);
                JFreeChart jfc = tp.createXYLineChart(trace.getDisplayName(), trace.getDisplayTimeUnit(), trace.getDisplayVoltageUnit());
                IntervalMarker marker = new IntervalMarker(trace.getTimeOnIndex(maxCorrIndex), trace.getTimeOnIndex(maxCorrIndex + characterWidths.get('X') * characterCounts.get('X')));
                marker.setPaint(TracePlotter.ORANGE);
                marker.setAlpha(0.2f);
                jfc.getXYPlot().addDomainMarker(marker, Layer.BACKGROUND);
                String imageName = FileUtils.saveComparisonImage(outputPath, jfc);
                System.out.println("Max corr index is: " + maxCorrIndex + " with corr: " + maxCorr);
                System.out.println("Image saved to: " + imageName);
            }
        } catch (Exception ex) {
            throw new ActionException(ex);
        }
    }

    private HashMap<Character, double[]> initializeCorrelations(HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals, int endingIndex) {
        HashMap<Character, double[]> correlations = new HashMap();
        for (Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals : maskIntervals.entrySet()) {
            correlations.put(characterIntervals.getKey(), new double[endingIndex]);
        }
        return correlations;
    }

    private int generateMaskIntervalsForTimes(COTemplateConfiguration coTemplateConfiguration, int samplingFrequency, int timeIteration, HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals, HashMap<Character, Integer> characterWidths, int lastIntervalIndex) {
        for (int maskLetterIndex = 0; maskLetterIndex < coTemplateConfiguration.mask.length(); maskLetterIndex++) { // For each mask letter create interval
            Character maskCharacter = coTemplateConfiguration.mask.charAt(maskLetterIndex);
            COTemplateMaskElement maskElement = coTemplateConfiguration.elements.stream().filter(e -> e.maskElement == maskCharacter).findFirst().orElse(null);
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
}
