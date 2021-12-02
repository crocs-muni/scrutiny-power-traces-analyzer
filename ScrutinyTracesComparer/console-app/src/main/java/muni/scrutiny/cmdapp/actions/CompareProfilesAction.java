package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.module.configurations.compared.ComparedCardConfigTrace;
import muni.scrutiny.module.configurations.compared.NewCardConfig;
import muni.scrutiny.module.configurations.compared.TraceComparisonResult;
import muni.scrutiny.module.configurations.compared.TracesComparisonResult;
import muni.scrutiny.module.configurations.reference.ReferenceCardConfig;
import muni.scrutiny.module.configurations.reference.ReferenceCardOperation;
import muni.scrutiny.module.configurations.reference.ReferenceMeasurements;
import muni.scrutiny.module.pipelines.base.PipelineFactory;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class CompareProfilesAction extends BaseAction {
    public static final String name = "compare";
    private static final String referenceProfileShort = "-r";
    private static final String newProfileShort = "-n";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public CompareProfilesAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(referenceProfileShort, new ActionParameter(new ArrayList<String>() {{
                add(referenceProfileShort);
            }}, true, null));
            put(newProfileShort, new ActionParameter(new ArrayList<String>() {{
                add(newProfileShort);
            }}, true, null));
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
    public void checkArguments() {
    }

    @Override
    public void executeAction(String[] arguments) throws ActionException {
        try {
            super.executeAction(arguments);
            System.out.println("Started profiles comparison");
            Path referenceProfilePath = getParameterAsPath(referenceProfileShort);
            System.out.println("Reference profile path:");
            System.out.println(referenceProfilePath.toAbsolutePath());
            Path newProfilePath = getParameterAsPath(newProfileShort);
            System.out.println("New profile path:");
            System.out.println(newProfilePath.toAbsolutePath());
            Path outputPath = newProfilePath.getParent();
            ReferenceCardConfig rcc = getReferenceCardConfig(referenceProfilePath);
            NewCardConfig ncc = getNewCardConfig(newProfilePath);
            List<File> referenceFiles = FileUtils.getFilesInDirectory(referenceProfilePath.getParent());
            List<File> newFiles = FileUtils.getFilesInDirectory(newProfilePath.getParent());
            TracesComparisonResult tcrs = new TracesComparisonResult();
            tcrs.tracesResults = new ArrayList<>();

            System.out.println("Starting comparison...");
            for (ReferenceCardOperation rco : rcc.operations) {
                System.out.println("Operation: " + rco.operationCode);
                Optional<ComparedCardConfigTrace> ct = ncc.traces.stream()
                        .filter((tr) -> tr.operationCode.equals(rco.operationCode))
                        .findFirst();
                if (!ct.isPresent()) {
                    tcrs.tracesResults.add(new TraceComparisonResult(rco.operationCode));
                    continue;
                }

                List<Path> referencePathsToOperations = FileUtils.getPathsToOperationTraces(referenceFiles, rco.filePaths, rco.operationCode);
                List<Trace> referenceOperationTraces = FileUtils.getOperationTraces(referencePathsToOperations);
                List<Path> newPathsToOperations = FileUtils.getPathsToOperationTraces(newFiles, rco.filePaths, rco.operationCode);
                List<Trace> newOperationTraces = FileUtils.getOperationTraces(newPathsToOperations);
                System.out.println("Reference traces found: " + referenceOperationTraces.size());
                System.out.println("New traces found: " + newOperationTraces.size());

                for (ReferenceMeasurements rm : rco.measurements) {
                    TraceComparisonResult tcr = new TraceComparisonResult();
                    tcr.operationPresent = true;
                    tcr.operationCode = rco.operationCode;
                    tcr.comparisonResults = new ArrayList<>();
                    tcr.charts = new ArrayList<>();
                    tcrs.tracesResults.add(tcr);
                    for (Trace ti : referenceOperationTraces) {
                        for (Trace tj : newOperationTraces) {
                            ComparisonPipeline cp = PipelineFactory.getInstance(
                                    rm.pipeline,
                                    ti.getSamplingFrequency(),
                                    ti.getMinimalVoltage(),
                                    ti.getMinimalVoltage(),
                                    rco.customParameters == null ? rcc.customParameters : rco.customParameters);
                            System.out.println("Comparing traces " + ti.getDisplayName() + " and " + tj.getDisplayName() + " with " + rm.pipeline);
                            long start = System.currentTimeMillis();
                            ComparisonResult cr = cp.compare(ti, tj);
                            long end = System.currentTimeMillis();
                            System.out.println("Comparison ended in: " + (double)(end-start)/1000 + "s");
                            String imageName = FileUtils.saveComparisonImage(outputPath, cr.getChart());
                            System.out.println("Saved image to: " + outputPath.resolve(imageName));
                            tcr.comparisonResults.add(cr.getBestSimilarity().getDistance());
                            tcr.charts.add(imageName);
                        }
                    }
                }
            }

            String tcrJson = new Gson().toJson(tcrs);
            try (PrintWriter out = new PrintWriter(outputPath.resolve("comparison_result.json").toFile())) {
                out.println(tcrJson);
            }

            System.out.println("Saved resulting comparison to: " + outputPath.resolve("comparison_result.json").toAbsolutePath());
        } catch (FileNotFoundException exception) {
            throw new ActionException(exception);
        } catch (IOException exception) {
            throw new ActionException(exception);
        }
    }

    private NewCardConfig getNewCardConfig(Path newProfilePath) throws ActionException {
        String newProfileContent = FileUtils.readFile(newProfilePath);
        return new Gson().fromJson(newProfileContent, NewCardConfig.class);
    }

    private ReferenceCardConfig getReferenceCardConfig(Path referenceProfilePath) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(referenceProfilePath);
        return new Gson().fromJson(referenceProfileContent, ReferenceCardConfig.class);
    }
}
