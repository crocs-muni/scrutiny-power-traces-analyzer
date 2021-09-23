package muni.scrutiny.charts.models;

import muni.scrutiny.charts.TracePlotter;

import java.awt.Color;

public class Boundary {
    private double lowerBound;
    private double upperBound;
    private int lowerBoundIndex;
    private int upperBoundIndex;
    private String name;
    private Color color;

    public Boundary(double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        setBounds(lowerBound, upperBound, lowerBoundIndex, upperBoundIndex);
        this.color = TracePlotter.RED;
    }

    public Boundary(String name, double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        setBounds(lowerBound, upperBound, lowerBoundIndex, upperBoundIndex);
        this.name = name;
    }

    public Boundary(Color color, double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        setBounds(lowerBound, upperBound, lowerBoundIndex, upperBoundIndex);
        this.color = color;
    }

    public Boundary(String name, Color color, double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        setBounds(lowerBound, upperBound, lowerBoundIndex, upperBoundIndex);
        this.name = name;
        this.color = color;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public int getLowerBoundIndex() {
        return lowerBoundIndex;
    }

    public void setLowerBoundIndex(int lowerBound) {
        this.lowerBoundIndex = lowerBoundIndex;
    }

    public int getUpperBoundIndex() {
        return upperBoundIndex;
    }

    public void setUpperBoundIndex(int upperBound) {
        this.upperBoundIndex = upperBoundIndex;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Color getColor() { return color; }

    public void setColor(Color color) { this.color = color; }

    private void setBounds(double lowerBound, double upperBound, int lowerBoundIndex, int upperBoundIndex) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerBoundIndex = lowerBoundIndex;
        this.upperBoundIndex = upperBoundIndex;
    }
}
