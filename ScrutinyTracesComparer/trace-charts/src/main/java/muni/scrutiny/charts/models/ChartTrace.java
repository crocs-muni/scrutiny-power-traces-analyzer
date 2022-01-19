package muni.scrutiny.charts.models;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.traces.models.Trace;

import java.awt.*;

public class ChartTrace {
    private final Trace trace;
    private final Color color;
    private final Stroke stroke;
    private final int indexOffset;

    public ChartTrace(Trace trace, Color color) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = 0;
        this.stroke = TracePlotter.basicChartStroke;
    }

    public ChartTrace(Trace trace, Color color, int indexOffset) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = indexOffset;
        this.stroke = TracePlotter.basicChartStroke;
    }

    public ChartTrace(Trace trace, Color color, Stroke stroke) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = 0;
        this.stroke = stroke;
    }

    public ChartTrace(Trace trace, Color color, int indexOffset, Stroke stroke) {
        this.trace = trace;
        this.color = color;
        this.indexOffset = indexOffset;
        this.stroke = stroke;
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

    public Stroke getStroke() { return this.stroke; }
}
