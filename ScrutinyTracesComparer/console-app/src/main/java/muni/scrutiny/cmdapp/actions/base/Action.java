package muni.scrutiny.cmdapp.actions.base;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Action {
    String getName();
    Map<String, ActionParameter> getActionParameters();
    Map<String, ActionFlag> getActionFlags();
    void checkArguments() throws ActionException;
    void executeAction(String[] arguments) throws ActionException;
}
