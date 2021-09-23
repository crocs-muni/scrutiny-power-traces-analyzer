package muni.scrutiny.charts.models;

import muni.scrutiny.traces.models.Trace;

import java.awt.Color;

public class ChartTrace {
    private final Trace trace;
    private final Color color;

    public ChartTrace(Trace trace, Color color) {
        this.trace = trace;
        this.color = color;
    }

    public Trace getTrace() {
        return this.trace;
    }

    public Color getColor() {
        return this.color;
    }
}
