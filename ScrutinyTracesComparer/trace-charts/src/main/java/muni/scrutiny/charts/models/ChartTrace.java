package muni.scrutiny.charts.models;

import muni.scrutiny.charts.TracePlotter;
import muni.scrutiny.traces.models.Trace;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ChartTrace {
    private Trace trace;
    private Color color;
    private Stroke stroke = new BasicStroke();
    private Shape shape = null;
    private int indexOffset = 0;
    private String displayName;
    private int order;
    private boolean shapesVisible = false;
    private boolean linesVisible = true;

    public ChartTrace(Trace trace, Color color) {
        this.trace = trace;
        this.color = color;
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
        this.stroke = stroke;
    }


    public ChartTrace(Trace trace, Color color, Stroke stroke, Shape shape) {
        this.trace = trace;
        this.color = color;
        this.stroke = stroke;
        this.shape = shape;
        shapesVisible = true;
    }

    public ChartTrace(Trace trace, Color color, Shape shape) {
        this.trace = trace;
        this.color = color;
        this.shape = shape;
        shapesVisible = true;
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

    public Shape getShape() { return this.shape; }

    public String getDisplayName() { return displayName; }

    public int getOrder() { return order; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public void setOrder(int order) { this.order = order; }
}
