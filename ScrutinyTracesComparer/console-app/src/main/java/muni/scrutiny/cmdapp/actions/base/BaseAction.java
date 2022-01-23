package muni.scrutiny.cmdapp.actions.base;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class BaseAction implements Action {
    @Override
    public void executeAction(String[] arguments) throws ActionException {
        parseArguments(arguments);
    }

    @Override
    public void checkArguments() throws ActionException {
        System.out.print("Checking arguments...");
        for (Map.Entry<String, ActionParameter> argument : getActionParameters().entrySet()) {
            if (argument.getValue().isMandatory() && argument.getValue().getValueOrDefault() == null) {
                throw new ActionException(argument.getKey() + " is a mandatory parameter");
            }
        }
        System.out.println("OK");
    }

    protected void parseArguments(String[] arguments) throws ActionException {
        System.out.print("Parsing arguments...");
        List<String> parametersCopy = Arrays.asList(arguments);
        List<Integer> indexesToDelete =  new ArrayList<>();
        parseParameters(parametersCopy, indexesToDelete);
        parseFlags(parametersCopy, indexesToDelete);
        System.out.println("OK");
        checkArguments();
    }

    protected Path getParameterAsPath(String parameterKey) {
        return Paths.get(getActionParameters().get(parameterKey).getValueOrDefault());
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
                if (parameter.isPresent() && (index) < parametersCopy.size()) {
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
