package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.similaritysearch.pipelines.base.PreprocessingResult;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import muni.scrutiny.traces.saver.DataSaver;
import muni.scrutiny.tracesconcat.configurations.TracesConcatConfig;
import muni.scrutiny.tracesconcat.pipelines.ConcatPreprocessingPipeline;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConcatTracesAction  extends BaseAction {
    public static final String name = "concat";
    private static final String concatConfigShort = "-c";
    private static final String concatOutputShort = "-o";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public ConcatTracesAction() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(concatConfigShort, new ActionParameter(new ArrayList<String>() {{
                add(concatConfigShort);
            }}, true, null));
            put(concatOutputShort, new ActionParameter(new ArrayList<String>() {{
                add(concatOutputShort);
            }}, false, null));
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
            Path tccPath = getParameterAsPath(concatConfigShort);
            TracesConcatConfig tcc = getCreateReferenceProfileConfig(tccPath);
            int lowestSamplingFreq = Integer.MAX_VALUE;
            for (int i = 0; i < tcc.tracePaths.size(); i++) {
                Path tracePath = tccPath.getParent().resolve(Paths.get(tcc.tracePaths.get(i)));
                int sf = DataManager.getSamplingFrequency(tracePath);
                if (sf < lowestSamplingFreq) {
                    lowestSamplingFreq = sf;
                }
            }

            double[] resultingVoltage = null;
            int dataCount = 0;
            String voltageUnit = null;
            String timeUnit = null;
            double referenceMinimum = Double.MAX_VALUE;
            for (int i = 0; i < tcc.tracePaths.size(); i++) {
                Path tracePath = tccPath.getParent().resolve(Paths.get(tcc.tracePaths.get(i)));
                Trace tNew = DataManager.loadTrace(tracePath, false);
                voltageUnit = tNew.getVoltageUnit();
                timeUnit = tNew.getTimeUnit();
                referenceMinimum = referenceMinimum == Double.MAX_VALUE ? tNew.getAverageOfFirstNValues(500) : referenceMinimum;
                ConcatPreprocessingPipeline cpp = new ConcatPreprocessingPipeline(lowestSamplingFreq, referenceMinimum);
                PreprocessingResult pr = cpp.preprocess(tNew);
                Trace ptNew = pr.getPreprocessedTrace();
                dataCount += ptNew.getDataCount();
                double[] tmpArray = new double[dataCount];
                if (resultingVoltage != null) {
                    System.arraycopy(resultingVoltage, 0, tmpArray, 0, resultingVoltage.length);
                    System.arraycopy(ptNew.getVoltage(), 0, tmpArray, resultingVoltage.length, ptNew.getDataCount());
                } else {
                    System.arraycopy(ptNew.getVoltage(), 0, tmpArray, 0, ptNew.getDataCount());
                }

                resultingVoltage = tmpArray;
            }

            Trace resultingTrace = new Trace("concat_trace.csv", dataCount, voltageUnit, timeUnit, resultingVoltage, lowestSamplingFreq);
            Path savingPath = parameters.get(concatOutputShort) == null ? tccPath.getParent() : getParameterAsPath(concatOutputShort);
            savingPath = savingPath.resolve(resultingTrace.getName());
            System.out.println("Saving to the path: " + savingPath.toAbsolutePath());
            DataSaver.exportToCsv(resultingTrace, savingPath);

        } catch (Exception exception) {
            throw new ActionException(exception);
        }
    }

    private TracesConcatConfig getCreateReferenceProfileConfig(Path traceConcatConfig) throws ActionException {
        String referenceProfileContent = FileUtils.readFile(traceConcatConfig);
        return new Gson().fromJson(referenceProfileContent, TracesConcatConfig.class);
    }
}
