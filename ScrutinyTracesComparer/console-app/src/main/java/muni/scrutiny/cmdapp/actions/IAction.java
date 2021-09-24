package muni.scrutiny.cmdapp.actions;

import muni.scrutiny.cmdapp.actions.models.ActionParameter;

import java.util.List;

public interface IAction {
    String getName();
    List<ActionParameter> getActionParameters();
}
