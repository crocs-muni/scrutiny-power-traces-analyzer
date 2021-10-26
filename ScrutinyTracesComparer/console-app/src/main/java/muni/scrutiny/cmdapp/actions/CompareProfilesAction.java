package muni.scrutiny.cmdapp.actions;

import com.google.gson.Gson;
import muni.scrutiny.cmdapp.actions.base.ActionFlag;
import muni.scrutiny.cmdapp.actions.base.ActionParameter;
import muni.scrutiny.cmdapp.actions.base.BaseAction;
import muni.scrutiny.configurations.input.compared.NewCardConfig;
import muni.scrutiny.configurations.input.reference.ReferenceCardConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompareProfilesAction extends BaseAction {
    private static final String referenceProfile = "-r";
    private static final String newProfile = "-n";

    private final String name;
    private final Map<String, ActionParameter> parameters;
    private final Map<String, ActionFlag> flags;

    public CompareProfilesAction() {
        this.name = "compare";
        parameters = new HashMap<String, ActionParameter>() {{
            put(referenceProfile, new ActionParameter(new ArrayList<String>() {{
                add("--reference-profile");
                add(referenceProfile);
            }}, true, null));
            put(newProfile, new ActionParameter(new ArrayList<String>() {{
                add("--new-profile");
                add(newProfile);
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
    public void executeAction(String[] arguments) throws IOException {
        parseArguments(arguments);
        Path referenceProfilePath = Paths.get(parameters.get(referenceProfile).getValueOrDefault());
        Path newProfilePath = Paths.get(parameters.get(newProfile).getValueOrDefault());
        String referenceProfileContent = readFile(referenceProfilePath);
        String newProfileContent = readFile(newProfilePath);
        ReferenceCardConfig referenceCardConfig = new Gson().fromJson(referenceProfileContent, ReferenceCardConfig.class);
        NewCardConfig newCardConfig = new Gson().fromJson(newProfileContent, NewCardConfig.class);

    }

    private static String readFile(Path path) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded);
    }
}
