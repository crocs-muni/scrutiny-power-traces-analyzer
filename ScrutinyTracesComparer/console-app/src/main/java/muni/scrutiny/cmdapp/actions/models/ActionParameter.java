package muni.scrutiny.cmdapp.actions.models;

import java.util.List;

public class ActionParameter<T> {
    private final List<String> names;
    private final boolean hasValue;
    private T value;

    public ActionParameter(List<String> names, boolean hasValue) {
        this.names = names;
        this.hasValue = hasValue;
        this.value = null;
    }

    public ActionParameter(List<String> names, boolean hasValue, T value) {
        this.names = names;
        this.hasValue = hasValue;
        this.value = value;
    }

    public List<String> getNames() {
        return names;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
