//package muni.scrutiny.cmdapp.actions;
//
//import com.google.gson.Gson;
//import muni.scrutiny.cmdapp.actions.base.ActionException;
//import muni.scrutiny.cmdapp.actions.base.ActionFlag;
//import muni.scrutiny.cmdapp.actions.base.ActionParameter;
//import muni.scrutiny.cmdapp.actions.base.BaseAction;
//import muni.scrutiny.module.configurations.input.compared.ComparedCardConfigTrace;
//import muni.scrutiny.module.configurations.input.compared.NewCardConfig;
//import muni.scrutiny.module.configurations.input.reference.ReferenceCardConfig;
//import muni.scrutiny.module.configurations.input.reference.ReferenceCardOperation;
//import muni.scrutiny.module.configurations.output.TraceComparisonResult;
//import muni.scrutiny.module.configurations.output.TracesComparisonResult;
//import muni.scrutiny.module.pipelines.base.PipelineFactory;
//import muni.scrutiny.similaritysearch.pipelines.base.ComparisonPipeline;
//import muni.scrutiny.traces.DataManager;
//import muni.scrutiny.traces.models.Trace;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//public class CompareProfilesAction extends BaseAction {
//    private static final String referenceProfileShort = "-r";
//    private static final String newProfileShort = "-n";
//    private static final String metricShort = "-m";
//    private static final String defaultMetric = "euclidean";
//
//    private final String name;
//    private final Map<String, ActionParameter> parameters;
//    private final Map<String, ActionFlag> flags;
//
//    public CompareProfilesAction() {
//        this.name = "compare";
//        parameters = new HashMap<String, ActionParameter>() {{
//            put(referenceProfileShort, new ActionParameter(new ArrayList<String>() {{
//                add(referenceProfileShort);
//            }}, true, null));
//            put(newProfileShort, new ActionParameter(new ArrayList<String>() {{
//                add(newProfileShort);
//            }}, true, null));
//            put(metricShort, new ActionParameter(new ArrayList<String>() {{
//                add(metricShort);
//            }}, false, defaultMetric));
//        }};
//        flags = new HashMap<>();
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public Map<String, ActionParameter> getActionParameters() {
//        return parameters;
//    }
//
//    @Override
//    public Map<String, ActionFlag> getActionFlags() {
//        return flags;
//    }
//
//    @Override
//    public void checkArguments() {
//    }
//
//    @Override
//    public void executeAction(String[] arguments) throws ActionException {
//        try {
//            super.executeAction(arguments);
//            Path referenceProfilePath = getParameterAsPath(referenceProfileShort);
//            Path newProfilePath = getParameterAsPath(newProfileShort);
//            ReferenceCardConfig rcc = getReferenceCardConfig(referenceProfilePath);
//            NewCardConfig ncc = getNewCardConfig(newProfilePath);
//            TracesComparisonResult tcr = new TracesComparisonResult();
//            tcr.metric = parameters.get(metricShort).getValueOrDefault();
//            for (ReferenceCardOperation rct : rcc.operations) {
//                Optional<ComparedCardConfigTrace> ct = ncc.traces.stream()
//                        .filter((tr) -> tr.code.equals(rct.primaryCodeName))
//                        .findFirst();
//                if (!ct.isPresent()) {
//                    tcr.tracesResults.add(new TraceComparisonResult(rct.primaryCodeName));
//                    continue;
//                }
//
//                Path unknownTracePath = Paths.get(newProfilePath.toFile().getParent(), ct.get().filepath);
//                Path referenceTracePath = Paths.get(newProfilePath.toFile().getParent(), rct.primaryCodeName);
//                Trace unknownTrace = DataManager.loadTrace(unknownTracePath, false);
//                Trace referenceTrace = DataManager.loadTrace(referenceTracePath, false);
//                ComparisonPipeline cp = PipelineFactory.getInstance(rcc.pipeline, referenceTrace.getSamplingFrequency(), referenceTrace.getMinimalVoltage(), referenceTrace.getMaximalVoltage(), rct.customParameters);
//                tcr.tracesResults.add(new TraceComparisonResult(
//                        rct.primaryCodeName,
//                        cp.compare(referenceTrace, unknownTrace).getBestSimilarity().getDistance()));
//            }
//
//            String tcrJson = new Gson().toJson(tcr);
//            String outputFilePath = "result.json";
//            try (PrintWriter out = new PrintWriter(outputFilePath)) {
//                out.println(tcrJson);
//            }
//        } catch (FileNotFoundException exception) {
//            throw new ActionException(exception);
//        } catch (IOException exception) {
//            throw new ActionException(exception);
//        }
//    }
//
//    private NewCardConfig getNewCardConfig(Path newProfilePath) throws ActionException {
//        String newProfileContent = readFile(newProfilePath);
//        return new Gson().fromJson(newProfileContent, NewCardConfig.class);
//    }
//
//    private ReferenceCardConfig getReferenceCardConfig(Path referenceProfilePath) throws ActionException {
//        String referenceProfileContent = readFile(referenceProfilePath);
//        return new Gson().fromJson(referenceProfileContent, ReferenceCardConfig.class);
//    }
//
//    private static String readFile(Path path) throws ActionException {
//        try {
//            byte[] encoded = Files.readAllBytes(path);
//            return new String(encoded);
//        } catch (IOException exception) {
//            throw new ActionException(exception);
//        }
//    }
//}
