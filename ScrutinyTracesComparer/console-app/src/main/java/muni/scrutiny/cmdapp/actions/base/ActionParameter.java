package muni.scrutiny.cmdapp.actions.base;

import java.util.List;

public class ActionParameter {
    private final List<String> names;
    private final String defaultValue;
    private final Boolean isMandatory;
    private String value;

    public ActionParameter(List<String> names, Boolean isMandatory, String defaultValue) {
        this.names = names;
        this.defaultValue = defaultValue;
        this.isMandatory = isMandatory;
        this.value = null;
    }

    public List<String> getNames() {
        return names;
    }

    public Boolean isMandatory() {
        return isMandatory;
    }

    public String getValueOrDefault()
    {
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
