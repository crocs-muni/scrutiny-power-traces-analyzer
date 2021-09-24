package muni.scrutiny.cmdapp.actions;

import muni.scrutiny.cmdapp.actions.models.ActionParameter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CompareProfilesAction implements IAction {
    private final String name;
    private final List<ActionParameter> parameters;

    public CompareProfilesAction() {
        this.name = "compare";
        parameters = new ArrayList<ActionParameter>() {{
            add(new ActionParameter<String>(new ArrayList<String>() {{
                add("--reference-profile");
                add("-r");
            }}, true));
            add(new ActionParameter<String>(new ArrayList<String>() {{
                add("--new-profile");
                add("-n");
            }}, true));
        }};
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<ActionParameter> getActionParameters() {
        return null;
    }
}
