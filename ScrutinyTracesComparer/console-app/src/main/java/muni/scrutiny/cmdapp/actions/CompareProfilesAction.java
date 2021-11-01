package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.configurations.input.compared.ComparedCardConfigTrace;
import muni.scrutiny.configurations.input.compared.NewCardConfig;
import muni.scrutiny.configurations.input.reference.ReferenceCardConfig;
import muni.scrutiny.configurations.input.reference.ReferenceCardConfigTrace;
import muni.scrutiny.configurations.output.TraceComparisonResult;
import muni.scrutiny.configurations.output.TracesComparisonResult;
import muni.scrutiny.similaritysearch.measures.DistanceMeasure;
import muni.scrutiny.similaritysearch.measures.EuclideanDistance;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompareProfilesAction extends BaseAction {
    private static final String referenceProfileShort = "-r";
    private static final String referenceProfileLong = "--reference-profile";
    private static final String newProfileShort = "-n";
    private static final String newProfileLong = "--new-profile";
    private static final String metricShort = "-m";
    private static final String metricLong = "--metric";
    private static final String defaultMetric = "euclidean";

    private final String name;
    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public CompareProfilesAction() {
        this.name = "compare";
        parameters = new HashMap<String, ActionParameter>() {{
            put(referenceProfileShort, new ActionParameter(new ArrayList<String>() {{
                add(referenceProfileShort);
                add(referenceProfileLong);
            }}, true, null));
            put(newProfileShort, new ActionParameter(new ArrayList<String>() {{
                add(newProfileShort);
                add(newProfileLong);
            }}, true, null));
            put(metricShort, new ActionParameter(new ArrayList<String>() {{
                add(metricShort);
                add(metricLong);
            }}, false, defaultMetric));
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
        super.executeAction(arguments);
        ReferenceCardConfig rcc = getReferenceCardConfig();
        NewCardConfig ncc = getNewCardConfig();
        TracesComparisonResult tcr = new TracesComparisonResult();
        tcr.metric = parameters.get(metricShort).getValueOrDefault();
//        for (ReferenceCardConfigTrace rct : rcc.traces) {
//            Optional<ComparedCardConfigTrace> ct = ncc.traces.stream()
//                    .filter((tr) -> tr.code.equals(rct.code))
//                    .findFirst();
//            if (!ct.isPresent()) {
//                tcr.tracesResults.add(new TraceComparisonResult(rct.code));
//                continue;
//            }
//
//            String uknownTracePath = ucDirectory + "\\" + ct.get().filepath;
//            String referenceTracePath = rcDirectory + "\\" + rct.filepath;
//            Trace unknownTrace = DataManager.loadTrace(Paths.get(uknownTracePath), false);
//            Trace referenceTrace = DataManager.loadTrace(Paths.get(referenceTracePath), false);
//            DistanceMeasure dm = new EuclideanDistance();
//            double[] utv = unknownTrace.getVoltage();
//            double[] rtv = referenceTrace.getVoltage();
//            tcr.tracesResults.add(new TraceComparisonResult(
//                    rct.code,
//                    dm.compute(utv.length > rtv.length ? rtv : utv, utv.length > rtv.length ? utv : rtv, 0)));
//        }

        String tcrJson = new Gson().toJson(tcr);
        String outputFilePath = "result.json";
        try (PrintWriter out = new PrintWriter(outputFilePath)) {
            out.println(tcrJson);
        } catch (FileNotFoundException exception) {
            throw new ActionException(exception);
        }
    }

    private NewCardConfig getNewCardConfig() throws ActionException {
        Path newProfilePath = Paths.get(parameters.get(newProfileShort).getValueOrDefault());
        String newProfileContent = readFile(newProfilePath);
        return new Gson().fromJson(newProfileContent, NewCardConfig.class);
    }

    private ReferenceCardConfig getReferenceCardConfig() throws ActionException {
        Path referenceProfilePath = Paths.get(parameters.get(referenceProfileShort).getValueOrDefault());
        String referenceProfileContent = readFile(referenceProfilePath);
        return new Gson().fromJson(referenceProfileContent, ReferenceCardConfig.class);
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
