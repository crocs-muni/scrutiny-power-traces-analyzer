package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.module.configurations.compared.input.ProfilesComparisonInputOperation;
import muni.scrutiny.module.configurations.compared.input.ProfilesComparisonInput;
import muni.scrutiny.module.configurations.compared.output.ProfilesComparisonOutputOperation;
import muni.scrutiny.module.configurations.compared.output.ProfilesComparisonOutput;
import muni.scrutiny.module.configurations.module.ScrutinyModule;
import muni.scrutiny.module.configurations.module.ScrutinyModules;
import muni.scrutiny.module.configurations.reference.output.CreateReferenceProfileOutput;
import muni.scrutiny.module.configurations.reference.output.CreateReferenceProfileOutputOperation;
import muni.scrutiny.module.configurations.reference.output.CreateReferenceProfileOutputMeasurements;
import muni.scrutiny.module.configurations.reference.output.CreateReferenceProfileOutputOperationExecTime;
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
            CreateReferenceProfileOutput rcc = getReferenceCardConfig(referenceProfilePath);
            ProfilesComparisonInput ncc = getNewCardConfig(newProfilePath);
            List<File> referenceFiles = FileUtils.getFilesInDirectory(referenceProfilePath.getParent());
            List<File> newFiles = FileUtils.getFilesInDirectory(newProfilePath.getParent());
            ProfilesComparisonOutput tcrs = new ProfilesComparisonOutput();
            tcrs.tracesResults = new ArrayList<>();

            System.out.println("Starting comparison...");
            for (CreateReferenceProfileOutputOperation rco : rcc.operations) {
                System.out.println("Operation: " + rco.operationCode);
                Optional<ProfilesComparisonInputOperation> ct = ncc.operations.stream()
                        .filter((tr) -> tr != null && rco != null && rco.operationCode != null && tr.operationCode != null && tr.operationCode.equals(rco.operationCode))
                        .findFirst();
                if (!ct.isPresent()) {
                    tcrs.tracesResults.add(new ProfilesComparisonOutputOperation(rco.operationCode));
                    continue;
                }

                List<Path> referencePathsToOperations = FileUtils.getPathsToOperationTraces(referenceFiles, rco.filePaths, rco.operationCode);
                List<Trace> referenceOperationTraces = FileUtils.getOperationTraces(referencePathsToOperations);
                List<Path> newPathsToOperations = FileUtils.getPathsToOperationTraces(newFiles, ct.get().filePaths, ct.get().operationCode);
                List<Trace> newOperationTraces = FileUtils.getOperationTraces(newPathsToOperations);
                System.out.println("Reference traces found: " + referenceOperationTraces.size());
                System.out.println("New traces found: " + newOperationTraces.size());

                for (CreateReferenceProfileOutputMeasurements rm : rco.measurements) {
                    ProfilesComparisonOutputOperation tcr = new ProfilesComparisonOutputOperation();
                    tcr.operationPresent = true;
                    tcr.operationCode = rco.operationCode;
                    tcr.executionTimes = newOperationTraces.stream().map(ot -> new CreateReferenceProfileOutputOperationExecTime(ot.getTimeUnit(), ot.getExecutionTime())).collect(Collectors.toList());
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
                            tcr.pipelineCode = rm.pipeline;
                            tcr.charts.add(imageName);
                        }
                    }
                }
            }

            try (PrintWriter out = new PrintWriter(outputPath.resolve("comparison_result.json").toFile())) {
                ScrutinyModules scrutinyModules = new ScrutinyModules();
                ScrutinyModule<ProfilesComparisonOutput> scrutinyModule = new ScrutinyModule<>();
                scrutinyModule.data = tcrs;
                scrutinyModules.modules.put("TRACES_COMPARER", scrutinyModule);
                out.println(new Gson().toJson(scrutinyModules));
            }

            System.out.println("Saved resulting comparison to: " + outputPath.resolve("comparison_result.json").toAbsolutePath());
        } catch (FileNotFoundException exception) {
            throw new ActionException(exception);
        } catch (IOException exception) {
            throw new ActionException(exception);
        }
    }

    private ProfilesComparisonInput getNewCardConfig(Path newProfilePath) throws ActionException {
        String newProfileContent = FileUtils.readFile(newProfilePath);
        return new Gson().fromJson(newProfileContent, ProfilesComparisonInput.class);
    }

    private CreateReferenceProfileOutput getReferenceCardConfig(Path referenceProfilePath) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(referenceProfilePath);
        ScrutinyModules sms = new Gson().fromJson(referenceProfileContent, ScrutinyModules.class);
        return new Gson().fromJson(sms.modules.get("TRACES_COMPARER").data.toString(), CreateReferenceProfileOutput.class);
    }
}
