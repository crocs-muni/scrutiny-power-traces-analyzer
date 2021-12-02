package muni.scrutiny.charts.models;

import muni.scrutiny.traces.models.Trace;

import java.awt.Color;

public class ChartTrace {
    private final Trace trace;
    private final Color color;
    private final int indexOffset;

    public ChartTrace(Trace trace, Color color) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = 0;
    }

    public ChartTrace(Trace trace, Color color, int indexOffset) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = indexOffset;
    }

    public Trace getTrace() {
        return this.trace;
    }

    public Color getColor() {
        return this.color;
    }

    public int getIndexOffset() {
        return this.indexOffset;
    }
}
