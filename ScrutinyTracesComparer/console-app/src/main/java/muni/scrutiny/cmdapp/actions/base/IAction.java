package muni.scrutiny.cmdapp.actions.base;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IAction {
    String getName();
    Map<String, ActionParameter> getActionParameters();
    Map<String, ActionFlag> getActionFlags();
    void checkArguments();
    void executeAction(String[] arguments) throws IOException;
}
