package muni.scrutiny.cmdapp.actions.base;

import java.util.*;

public abstract class BaseAction implements IAction {
    protected void parseArguments(String[] arguments) {
        List<String> parametersCopy = Arrays.asList(arguments);
        List<Integer> indexesToDelete =  new ArrayList<>();
        parseParameters(parametersCopy, indexesToDelete);
        parseFlags(parametersCopy, indexesToDelete);
        checkArguments();
    }

    private void parseFlags(List<String> parametersCopy, List<Integer> indexesToDelete) {
        for (Map.Entry<String, ActionFlag> argument : getActionFlags().entrySet()) {
            for (int i = 0; i < parametersCopy.size(); i++) {
                int index = i;
                Optional<String> parameter = argument
                        .getValue()
                        .getNames()
                        .stream()
                        .filter(name -> parametersCopy.get(index).equals(name))
                        .findFirst();
                if (parameter.isPresent() && (index + 1) < parametersCopy.size()) {
                    argument.getValue().setValue(true);
                    break;
                }
            }

            deleteUsedArguments(parametersCopy, indexesToDelete);
        }
    }

    private void parseParameters(List<String> parametersCopy, List<Integer> indexesToDelete) {
        for (Map.Entry<String, ActionParameter> argument : getActionParameters().entrySet()) {
            for (int i = 0; i < parametersCopy.size(); i++) {
                int index = i;
                Optional<String> parameter = argument
                        .getValue()
                        .getNames()
                        .stream()
                        .filter(name -> parametersCopy.get(index).equals(name))
                        .findFirst();
                if (parameter.isPresent() && (index + 1) < parametersCopy.size()) {
                    argument.getValue().setValue(parametersCopy.get(index + 1));
                    break;
                }
            }

            deleteUsedArguments(parametersCopy, indexesToDelete);
        }
    }

    private void deleteUsedArguments(List<String> parametersCopy, List<Integer> indexesToDelete) {
        for (int index : indexesToDelete) {
            parametersCopy.remove(index);
        }
    }
}
