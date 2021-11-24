package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.module.configurations.input.reference.*;
import muni.scrutiny.module.pipelines.base.PipelineFactory;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class CreateReferenceProfileAction extends BaseAction {
    private static final String configShort = "-c";
    private static final String outputFolderShort = "-o";

    public static final String name = "createref";
    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public CreateReferenceProfileAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(configShort, new ActionParameter(new ArrayList<String>() {{
                add(configShort);
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

            Path referenceConfigPath = getParameterAsPath(configShort);
            Path outputPath = getParameterAsPath(outputFolderShort);
            CreateReferenceProfileConfig crpc = getCreateReferenceProfileConfig(referenceConfigPath);
            List<File> files = getFilesInDirectory(referenceConfigPath);
            ReferenceCardConfig rcc = new ReferenceCardConfig();
            rcc.cardCode = crpc.cardCode;
            rcc.customParameters = crpc.customParameters;
            rcc.operations = new ArrayList<>();
            for (CreateReferenceProfileOperation crpo : crpc.operations) {
                List<Path> pathsToOperations = getPathsToOperationTraces(files, crpo);
                List<Trace> operationTraces = getOperationTraces(pathsToOperations);
                ReferenceCardOperation rco = new ReferenceCardOperation();
                rco.operationCode = crpo.operationCode;
                rco.customParameters = crpo.customParameters;
                rco.filePaths = pathsToOperations.stream().map(pto -> pto.getFileName().toString()).collect(Collectors.toList());
                rco.executionTimes = operationTraces.stream().map(ot -> new ReferenceCardOperationExecTime(ot.getTimeUnit(), ot.getExecutionTime())).collect(Collectors.toList());
                rco.measurements = new ArrayList<>();
                for (String pipelineName : crpc.pipelines) {
                    ReferenceMeasurements rm = new ReferenceMeasurements();
                    rm.pipeline = pipelineName;
                    rm.distances = new ArrayList<>();
                    rco.measurements.add(rm);
                    for (int i = 0; i < operationTraces.size(); i++) {
                        for (int j = i; j < operationTraces.size(); j++) {
                            if (i != j) {
                                Trace ti = operationTraces.get(i);
                                Trace tj = operationTraces.get(j);
                                ComparisonPipeline cp = PipelineFactory.getInstance(
                                        pipelineName,
                                        ti.getSamplingFrequency(),
                                        ti.getMinimalVoltage(),
                                        ti.getMinimalVoltage(),
                                        crpo.customParameters == null ? crpc.customParameters : crpo.customParameters);
                                ComparisonResult tcr = cp.compare(ti, tj);
                                rm.distances.add(tcr.getBestSimilarity().getDistance());
                            }
                        }
                    }
                }

                for (Path pto : pathsToOperations) {
                    Path copied = Paths.get(outputPath.toAbsolutePath() + pto.getFileName().toString());
                    Files.copy(pto, copied, StandardCopyOption.REPLACE_EXISTING);
                }

                try (PrintWriter out = new PrintWriter(outputPath.toAbsolutePath() + "reference.json")) {
                    out.println(new Gson().toJson(rco));
                }
            }
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private List<File> getFilesInDirectory(Path referenceConfigPath) {
        List<File> files = Arrays.stream(referenceConfigPath.getParent().toFile().listFiles())
                .filter(f -> f.isFile())
                .collect(Collectors.toList());
        return files;
    }

    private List<Path> getPathsToOperationTraces(List<File> files, CreateReferenceProfileOperation crpo) {
        List<Path> pathsToOperations;
        if (crpo.arePathsSpecified()) {
            pathsToOperations = crpo.filePaths.stream().map(sp -> Paths.get(sp)).collect(Collectors.toList());
        } else {
            pathsToOperations = files
                    .stream()
                    .filter(f -> f.getName().contains(crpo.operationCode))
                    .map(f -> f.toPath())
                    .collect(Collectors.toList());
        }

        return pathsToOperations;
    }

    private List<Trace> getOperationTraces(List<Path> pathsToOperations) throws IOException {
        List<Trace> operationTrace = new ArrayList<>();
        for (Path path : pathsToOperations) {
            operationTrace.add(DataManager.loadTrace(path, false));
        }

        return operationTrace;
    }

    private CreateReferenceProfileConfig getCreateReferenceProfileConfig(Path referenceConfig) throws ActionException {
        String referenceProfileContent = readFile(referenceConfig);
        return new Gson().fromJson(referenceProfileContent, CreateReferenceProfileConfig.class);
    }

    private static String readFile(Path path) throws ActionException {
        try {
            byte[] encoded = Files.readAllBytes(path);
            return new String(encoded);
        } catch (IOException exception) {
            throw new ActionException(exception);
        }
    }
}
