package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.charts.models.ChartTrace;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.module.configurations.reference.*;
import muni.scrutiny.module.pipelines.base.PipelineFactory;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
import muni.scrutiny.similaritysearch.pipelines.base.ComparisonResult;
import muni.scrutiny.traces.models.Trace;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            put(outputFolderShort, new ActionParameter(new ArrayList<String>() {{
                add(outputFolderShort);
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
    public void checkArguments() throws ActionException {
        super.checkArguments();
    }

    @Override
    public void executeAction(String[] arguments) throws ActionException {
        try {
            super.executeAction(arguments);
            System.out.println("Started reference profile creation");
            Path referenceConfigPath = getParameterAsPath(configShort);
            System.out.println("Reference profile path:");
            System.out.println(referenceConfigPath.toAbsolutePath());
            Path outputPath = getParameterAsPath(outputFolderShort);
            System.out.println("Output path:");
            System.out.println(outputPath.toAbsolutePath());
            CreateReferenceProfileConfig crpc = getCreateReferenceProfileConfig(referenceConfigPath);
            List<File> files = FileUtils.getFilesInDirectory(referenceConfigPath.getParent());
            ReferenceCardConfig rcc = new ReferenceCardConfig();
            rcc.cardCode = crpc.cardCode;
            rcc.customParameters = crpc.customParameters;
            rcc.operations = new ArrayList<>();

            System.out.println("Starting creation of reference profile...");
            for (CreateReferenceProfileOperation crpo : crpc.operations) {
                System.out.println("Operation: " + crpo.operationCode);
                List<Path> pathsToOperations = FileUtils.getPathsToOperationTraces(files, crpo.filePaths, crpo.operationCode);
                List<Trace> operationTraces = FileUtils.getOperationTraces(pathsToOperations);
                System.out.println("Operation traces found: " + operationTraces.size());
                ReferenceCardOperation rco = new ReferenceCardOperation();
                rcc.operations.add(rco);
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
                                System.out.println("Comparing traces " + ti.getDisplayName() + " and " + tj.getDisplayName() + " with " + pipelineName);
                                long start = System.currentTimeMillis();
                                ComparisonResult cr = cp.compare(ti, tj);
                                long end = System.currentTimeMillis();
                                System.out.println("Comparison ended in: " + (double)(end-start)/1000 + "s");
                                String imageName = FileUtils.saveComparisonImage(outputPath, cr.getChart());
                                System.out.println("Saved image to: " + outputPath.resolve(imageName));
                                rm.distances.add(cr.getBestSimilarity().getDistance());
                            }
                        }
                    }
                }

                for (Path pto : pathsToOperations) {
                    Path copied = outputPath.resolve(pto.getFileName());
                    Files.copy(pto, copied, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (PrintWriter out = new PrintWriter(outputPath.resolve("reference.json").toFile())) {
                out.println(new Gson().toJson(rcc));
            }

            System.out.println("Saved resulting comparison to: " + outputPath.resolve("reference.json").toAbsolutePath());
        } catch (IOException ex) {
            throw new ActionException(ex);
        }
    }

    private CreateReferenceProfileConfig getCreateReferenceProfileConfig(Path referenceConfig) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(referenceConfig);
        return new Gson().fromJson(referenceProfileContent, CreateReferenceProfileConfig.class);
    }
}
