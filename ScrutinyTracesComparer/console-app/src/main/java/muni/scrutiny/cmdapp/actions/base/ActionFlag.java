package muni.scrutiny.cmdapp.actions.base;

import java.util.List;

public class ActionFlag {
    private final List<String> names;
    private final Boolean defaultValue;
    private Boolean value;

    public ActionFlag(List<String> names, Boolean defaultValue) {
        this.names = names;
        this.defaultValue = defaultValue;
        this.value = null;
    }

    public List<String> getNames() {
        return names;
    }

    public Boolean getValueOrDefault()
    {
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
