package muni.scrutiny.testinggui.models;

import muni.scrutiny.traces.models.Trace;

public class VisualizationTabModel implements IModel {
    private Trace beforeTrace;
    private Trace afterTrace;

    @Override
    public void clear() {
        beforeTrace = null;
        afterTrace = null;
        System.gc();
    }
}
