package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.dbclassifier.computing.OperationFinder;
import muni.scrutiny.dbclassifier.computing.cpu.CPUOperationFinder;
import muni.scrutiny.dbclassifier.computing.gpu.GPUOperationFinder;
import muni.scrutiny.dbclassifier.computing.models.OperationFinderResult;
import muni.scrutiny.dbclassifier.configurations.input.DBClassifierConfiguration;
import muni.scrutiny.dbclassifier.configurations.output.CardDBCResult;
import muni.scrutiny.dbclassifier.configurations.output.DBClassifierOutput;
import muni.scrutiny.dbclassifier.configurations.output.OperationDBCResult;
import muni.scrutiny.module.configurations.module.TracesComparerDevice;
import muni.scrutiny.module.configurations.output.TCOOperation;
import muni.scrutiny.module.configurations.output.TracesComparerOutput;
import muni.scrutiny.module.pipelines.base.PipelineFactory;
import muni.scrutiny.similaritysearch.collections.SimilaritySet;
import muni.scrutiny.similaritysearch.collections.SimilaritySetType;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TraceClassifierAction extends BaseAction {
    public static final String name = "classify";

    private static final String defaultPipeline = "pep";
    private static final String visualizationFormat = "%s_card_%s_operation_trace_to_classify";

    private static final String traceShort = "-t";
    private static final String dbConfigShort = "-c";
    private static final String graphicCardShort = "-g";
    private static final String pShort = "-p";
    private static final String takeNthShort = "-n";
    private static final String debugShort = "-d";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public TraceClassifierAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(traceShort, new ActionParameter(new ArrayList<String>() {{
                add(traceShort);
            }}, true, null));
            put(dbConfigShort, new ActionParameter(new ArrayList<String>() {{
                add(dbConfigShort);
            }}, true, null));
            put(pShort, new ActionParameter(new ArrayList<String>() {{
                add(pShort);
            }}, false, "0.99"));
            put(takeNthShort, new ActionParameter(new ArrayList<String>() {{
                add(takeNthShort);
            }}, false, "10"));
        }};
        flags = new HashMap<String, ActionFlag>() {{
            put(graphicCardShort, new ActionFlag(new ArrayList<String>() {{
                add(graphicCardShort);
            }}, false));
            put(debugShort, new ActionFlag(new ArrayList<String>() {{
                add(debugShort);
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
            double p = getParameterAsDouble(pShort);
            int takeNth = getParameterAsInt(takeNthShort);
            Path configPath = getParameterAsPath(dbConfigShort);
            Path outputPath = configPath.getParent();
            DBClassifierConfiguration dbcc = getDBClassifierConfiguration(configPath);
            DBClassifierOutput dbco = new DBClassifierOutput();
            dbco.cardResults = new ArrayList<>();
            Path traceToClassifyPath = getParameterAsPath(traceShort);
            Trace traceToClassify = DataManager.loadTrace(traceToClassifyPath, false);
            System.out.println("Trace to classify path: " + traceToClassifyPath.toAbsolutePath());
            System.out.println("Config path: " + configPath.toAbsolutePath());
            System.out.println("Output path: " + outputPath.toAbsolutePath());
            System.out.println("p=" + p);
            System.out.println("n=" + takeNth);

            System.out.println("Trace classification process started.");
            for (Path referenceProfilePath : dbcc.getPaths()) {
                TracesComparerOutput tco = getReferenceCardConfig(referenceProfilePath);
                long cardStart = System.currentTimeMillis();
                System.out.println("Using database of card " + tco.cardCode + " from path: " + referenceProfilePath.toAbsolutePath());
                Path referencePathFolder = referenceProfilePath.getParent();
                CardDBCResult cdbcr = new CardDBCResult();
                cdbcr.cardCode = tco.cardCode;
                cdbcr.operationResults = new ArrayList<>();
                for (TCOOperation operation : tco.results) {
                    System.out.println("Trying to find operation: " + operation.operationCode);
                    SimilaritySet similaritySet = new SimilaritySet(
                            SimilaritySetType.DISTANCE,
                            0,
                            operation.getConfidenceIntervalUpperBound(defaultPipeline, p));
                    double averageExecutionTime = operation.executionTimes.stream().mapToDouble(et -> et.time).average().orElse(0);
                    for (String operationTraceFileName : operation.operationTracesPaths) {
                        System.out.println("Using reference operation: " + operationTraceFileName);
                        long refOperationStart = System.currentTimeMillis();
                        Path traceFilePath = referencePathFolder.resolve(operationTraceFileName);
                        Trace operationReferenceTrace = DataManager.loadTrace(traceFilePath, false);
                        ComparisonPipeline cp = PipelineFactory.getInstance(
                                defaultPipeline,
                                operationReferenceTrace.getSamplingFrequency(),
                                operationReferenceTrace.getMinimalVoltage(),
                                operationReferenceTrace.getMaximalVoltage(),
                                null);
                        Trace preprocessedTraceToClassify = cp.preprocess(traceToClassify).getPreprocessedTrace();
                        preprocessedTraceToClassify.getTime(true);
                        OperationFinder of = null;
                        if (getActionFlags().get(graphicCardShort).getValueOrDefault()) {
                            of = new GPUOperationFinder();
                        } else {
                            of = new CPUOperationFinder();
                        }

                        OperationFinderResult ofr = of.findOperations(preprocessedTraceToClassify, operationReferenceTrace, takeNth);
                        int executionTimeIndexes = preprocessedTraceToClassify.getIndexOfTimeValue(averageExecutionTime);
                        similaritySet.addRange(ofr.distances, executionTimeIndexes, (double)traceToClassify.getSamplingFrequency()/preprocessedTraceToClassify.getSamplingFrequency());
                        saveComparisonInfo(outputPath, traceToClassify, tco, operation, operationTraceFileName, preprocessedTraceToClassify, ofr);
                        long refOperationEnd = System.currentTimeMillis();
                        System.out.println("Search for operation " + operationTraceFileName + " ended in: " + (double)(refOperationEnd- refOperationStart)/1000 + "s");
                    }

                    OperationDBCResult odbcr = new OperationDBCResult();
                    odbcr.startingTimes = similaritySet.getSimilarities().stream()
                            .mapToDouble(s -> traceToClassify.getNormalizedTimeOnIndex(s.getFirstIndex()))
                            .boxed()
                            .collect(Collectors.toList());
                    odbcr.distances = similaritySet.getSimilarities().stream()
                            .mapToDouble(s -> s.getDistance())
                            .boxed()
                            .collect(Collectors.toList());
                    odbcr.visualizedOperations = visualizeResultIfAny(outputPath, traceToClassify, tco, operation, averageExecutionTime, odbcr);
                    odbcr.operationCode = operation.operationCode;
                    cdbcr.operationResults.add(odbcr);
                    System.out.println(odbcr.startingTimes.size() + " operations " +  operation.operationCode + " found.");
                }

                long cardEnd = System.currentTimeMillis();
                System.out.println("Search for operation on card " + tco.cardCode + " ended in: " + (double)(cardEnd- cardStart)/1000 + "s");
            }

            try (PrintWriter out = new PrintWriter(outputPath.resolve("traceclassifier_result.json").toFile())) {
                out.println(new Gson().toJson(dbco));
            }
        } catch (Exception exception) {
            throw new ActionException(exception);
        }
    }

    private void saveComparisonInfo(Path outputPath, Trace traceToClassify, TracesComparerOutput tco, TCOOperation operation, String operationTraceFileName, Trace preprocessedTraceToClassify, OperationFinderResult ofr) throws IOException {
        if (getActionFlags().get(debugShort).getValueOrDefault()) {
            Trace t = new Trace(
                    "Distances_" + tco.cardCode + operation.operationCode + operationTraceFileName + ".csv",
                    preprocessedTraceToClassify.getDataCount(),
                    traceToClassify.getVoltageUnit(),
                    traceToClassify.getTimeUnit(),
                    ofr.distances,
                    traceToClassify.getSamplingFrequency());
            DataManager.saveTrace(outputPath.resolve(t.getName()), t);
            TracePlotter tp = new TracePlotter(new ChartTrace(t, TracePlotter.BLUE, TracePlotter.thinChartStroke));
            JFreeChart jfc = tp.createXYLineChart(t.getDisplayName(), t.getDisplayTimeUnit(), t.getDisplayVoltageUnit());
            FileUtils.saveComparisonImage(outputPath, jfc);
        }
    }

    private String visualizeResultIfAny(Path outputPath, Trace traceToClassify, TracesComparerOutput tco, TCOOperation operation, double averageExecutionTime, OperationDBCResult odbcr) throws IOException {
        if (odbcr.startingTimes.size() > 0) {
            TracePlotter tp = new TracePlotter(traceToClassify);
            JFreeChart jfc = tp.createXYLineChart(
                    String.format(visualizationFormat, tco.cardCode, operation.operationCode),
                    traceToClassify.getDisplayTimeUnit(),
                    traceToClassify.getDisplayVoltageUnit());
            XYPlot plot = jfc.getXYPlot();
            for (int i = 0; i < odbcr.distances.size(); i++) {
                IntervalMarker im = new IntervalMarker(
                        odbcr.startingTimes.get(i),
                        odbcr.startingTimes.get(i) + averageExecutionTime,
                        TracePlotter.ORANGE);
                im.setAlpha(0.2f);
                plot.addDomainMarker(im);
            }

            return FileUtils.saveComparisonImage(outputPath, jfc);
        }

        return null;
    }

    private DBClassifierConfiguration getDBClassifierConfiguration(Path dbClassifierConfigurationPath) throws ActionException {
        String dbClassifierConfigContent = FileUtils.readFile(dbClassifierConfigurationPath);
        DBClassifierConfiguration dbcc = new Gson().fromJson(dbClassifierConfigContent, DBClassifierConfiguration.class);
        return dbcc;
    }

    private TracesComparerOutput getReferenceCardConfig(Path referenceProfilePath) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(referenceProfilePath);
        TracesComparerDevice sms = new Gson().fromJson(referenceProfileContent, TracesComparerDevice.class);
        return sms.modules.get("TRACES_COMPARER").moduleData;
    }
}
