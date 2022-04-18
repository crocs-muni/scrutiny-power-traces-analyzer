package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.module.configurations.input.TCIOperation;
import muni.scrutiny.module.configurations.input.TracesComparerInput;
import muni.scrutiny.module.configurations.output.*;
import muni.scrutiny.module.configurations.module.TracesComparerModule;
import muni.scrutiny.module.configurations.module.TracesComparerDevice;
import muni.scrutiny.module.pipelines.base.PipelineFactory;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.traces.models.Trace;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
            TracesComparerOutput rcc = getReferenceCardConfig(referenceProfilePath);
            TracesComparerInput ncc = getNewCardConfig(newProfilePath);
            List<File> referenceFiles = FileUtils.getFilesInDirectory(referenceProfilePath.getParent());
            List<File> newFiles = FileUtils.getFilesInDirectory(newProfilePath.getParent());
            TracesComparerOutput tcrs = new TracesComparerOutput();
            tcrs.results = new ArrayList<>();

            System.out.println("Starting comparison...");
            for (TCOOperation rco : rcc.results) {
                System.out.println("Operation: " + rco.operationCode);
                Optional<TCIOperation> ct = ncc.operations.stream()
                        .filter((tr) -> tr != null && tr.operationCode != null && tr.operationCode.equals(rco.operationCode))
                        .findFirst();
                if (!ct.isPresent()) {
                    TCOOperation tcoOperation = new TCOOperation();
                    tcoOperation.operationCode = rco.operationCode;
                    tcoOperation.operationPresent = false;
                    tcrs.results.add(tcoOperation);
                    continue;
                }

                List<Path> referencePathsToOperations = FileUtils.getPathsToOperationTraces(referenceFiles, rco.operationTracesPaths, rco.operationCode);
                List<Trace> referenceOperationTraces = FileUtils.getOperationTraces(referencePathsToOperations);
                List<Path> newPathsToOperations = FileUtils.getPathsToOperationTraces(newFiles, ct.get().filePaths, ct.get().operationCode);
                List<Trace> newOperationTraces = FileUtils.getOperationTraces(newPathsToOperations);
                System.out.println("Reference traces found: " + referenceOperationTraces.size());
                System.out.println("New traces found: " + newOperationTraces.size());
                TCOOperation tcr = new TCOOperation();
                tcr.operationPresent = true;
                tcr.operationCode = rco.operationCode;
                tcr.executionTimes = newOperationTraces.stream().map(ot -> {
                    TCOOperationExecTime tcooet = new TCOOperationExecTime();
                    tcooet.time = ot.getExecutionTime();
                    tcooet.unit = ot.getTimeUnit();
                    return tcooet;
                }).collect(Collectors.toList());
                tcr.pipelineComparisonResults = new ArrayList<>();
                tcrs.results.add(tcr);

                for (TCOOperationPipelineComparisons rm : rco.pipelineComparisonResults) {
                    TCOOperationPipelineComparisons tcoopc = new TCOOperationPipelineComparisons();
                    tcoopc.pipeline = rm.pipeline;
                    tcoopc.comparisons = new ArrayList<>();
                    tcr.pipelineComparisonResults.add(tcoopc);
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
                            TCOComparison tcoc = new TCOComparison();
                            tcoc.file_path = imageName;
                            tcoc.distance = cr.getBestSimilarity().getDistance();
                            tcoopc.comparisons.add(tcoc);
                            tcoopc.metricType = cp.getMetricType();
                        }
                    }
                }
            }

            try (PrintWriter out = new PrintWriter(outputPath.resolve("comparison_result.json").toFile())) {
                TracesComparerDevice scrutinyModules = new TracesComparerDevice();
                scrutinyModules.name = ncc.cardCode;
                TracesComparerModule scrutinyModule = new TracesComparerModule();
                scrutinyModule.moduleData = tcrs;
                scrutinyModules.modules.put("TRACES_COMPARER", scrutinyModule);
                out.println(new Gson().toJson(scrutinyModules));
            }

            System.out.println("Saved resulting comparison to: " + outputPath.resolve("comparison_result.json").toAbsolutePath());
        } catch (IOException exception) {
            throw new ActionException(exception);
        }
    }

    private TracesComparerInput getNewCardConfig(Path newProfilePath) throws ActionException {
        String newProfileContent = FileUtils.readFile(newProfilePath);
        return new Gson().fromJson(newProfileContent, TracesComparerInput.class);
    }

    private TracesComparerOutput getReferenceCardConfig(Path referenceProfilePath) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(referenceProfilePath);
        TracesComparerDevice sms = new Gson().fromJson(referenceProfileContent, TracesComparerDevice.class);
        return sms.modules.get("TRACES_COMPARER").moduleData;
    }
}
