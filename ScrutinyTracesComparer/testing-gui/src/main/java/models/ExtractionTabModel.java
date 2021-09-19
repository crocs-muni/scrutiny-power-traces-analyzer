package models;

import muni.scrutiny.models.Trace;

public class ExtractionTabModel implements IModel {
    private Trace currentTrace;
    private Integer firstIndexOnChartTrace;
    private Integer lastIndexOnChartTrace;

    public ExtractionTabModel() {
        this.currentTrace = null;
        this.firstIndexOnChartTrace = 0;
        this.lastIndexOnChartTrace = 0;
    }

    public Trace getCurrentTrace() {
        return currentTrace;
    }

    public void setCurrentTrace(Trace currentTrace) {
        this.currentTrace = currentTrace;
    }

    public Integer getFirstIndexOnChartTrace() {
        return this.firstIndexOnChartTrace;
    }

    public void setFirstIndexOnChartTrace(Integer firstIndexOnChartTrace) {
        this.firstIndexOnChartTrace = firstIndexOnChartTrace;
    }

    public Integer getLastIndexOnChartTrace() {
        return this.lastIndexOnChartTrace;
    }

    public void setLastIndexOnChartTrace(Integer lastIndexOnChartTrace) {
        this.lastIndexOnChartTrace = lastIndexOnChartTrace;
    }

    @Override
    public void clear() {
        currentTrace = null;
        firstIndexOnChartTrace = 0;
        lastIndexOnChartTrace = 0;
        System.gc();
    }
}
