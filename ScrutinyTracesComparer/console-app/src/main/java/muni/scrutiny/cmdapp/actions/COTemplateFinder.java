package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.cotemplate.module.configurations.COTemplateConfiguration;
import muni.cotemplate.module.configurations.COTemplateMaskElement;
import muni.scrutiny.cmdapp.actions.base.ActionException;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.cmdapp.actions.utils.FileUtils;
import muni.scrutiny.traces.DataManager;
import muni.scrutiny.traces.models.Trace;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class COTemplateFinder extends BaseAction {
    public static final String name = "cotemp";
    private static final String tracePathShort = "-t";
    private static final String configShort = "-c";

    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public COTemplateFinder() {
        parameters = new HashMap<String, ActionParameter>() {{
            put(tracePathShort, new ActionParameter(new ArrayList<String>() {{
                add(tracePathShort);
            }}, true, null));
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
    public void executeAction(String[] arguments) throws ActionException {
        try {
            super.executeAction(arguments);
            Path tracePath = getParameterAsPath(tracePathShort);
            Trace trace = DataManager.loadTrace(tracePath, false);
            int samplingFrequency = trace.getSamplingFrequency();
            COTemplateConfiguration coTemplateConfiguration = getCOTemplateConfig(getParameterAsPath(configShort));
            if (!areTimeLengthsEqual(coTemplateConfiguration)) {
                throw new ActionException("Times sizes are not equal.");
            }

            double[] voltage = trace.getVoltage();

            int timeIterations = coTemplateConfiguration.elements.stream().findFirst().orElse(new COTemplateMaskElement()).times.size();
            for (int i = 0; i < timeIterations; i++) {
                HashMap<Character, List<Pair<Integer, Integer>>> maskIntervals = new HashMap<>();
                int lastIntervalIndex = 0;
                for (int j = 0; j < coTemplateConfiguration.mask.length(); j++) {
                    Character maskCharacter = coTemplateConfiguration.mask.charAt(j);
                    if (!maskIntervals.containsKey(coTemplateConfiguration.mask.charAt(i))) {
                        List<Pair<Integer, Integer>> elementIntervals = new ArrayList<>();
                        COTemplateMaskElement maskElement = coTemplateConfiguration.elements.stream().filter(e -> e.maskElement == maskCharacter).findFirst().orElse(null);
                        int dataLengthFromOperationTime = (int)(samplingFrequency * maskElement.times.get(i));
                        elementIntervals.add(Pair.of(lastIntervalIndex, dataLengthFromOperationTime));
                        lastIntervalIndex = dataLengthFromOperationTime + 1;
                        maskIntervals.put(maskCharacter, elementIntervals);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ActionException(ex);
        }
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
