package muni.scrutiny.traces.models;

import muni.scrutiny.traces.helpers.UnitsHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class represents trace consisting of time and voltage.
 * 
 * @author Martin Podhora
 */
public class Trace {
    private final String name;

    private final double[] voltageArray;

    private final double[] timeArray;

    private final int dataCount;

    private final String voltageUnit;

    private final String timeUnit;
    
    private double voltageMaximum;
    
    private double voltageMinimum;

    public Trace(
            String name,
            int dataCount,
            String voltageUnit,
            String timeUnit) {
        this.name = name;
        this.voltageUnit = voltageUnit;
        this.timeUnit = timeUnit;
        this.dataCount = dataCount;
        this.voltageArray = new double[dataCount];
        this.timeArray = new double[dataCount];
        this.voltageMaximum = Double.NEGATIVE_INFINITY;
        this.voltageMinimum = Double.POSITIVE_INFINITY;
    }
    
    public Trace(
            String name,
            String voltageUnit,
            String timeUnit,
            int dataCount,
            double[] voltageArray,
            double[] timeArray,
            double voltageMaximum,
            double voltageMinimum) {
        this.name = name;
        this.voltageUnit = voltageUnit;
        this.timeUnit = timeUnit;
        this.dataCount = dataCount;
        this.voltageArray = voltageArray;
        this.timeArray = timeArray;
        this.voltageMaximum = voltageMaximum;
        this.voltageMinimum = voltageMinimum;
    }

    public void addData(double voltageValue, double timeValue, int position) {
        voltageArray[position] = voltageValue;
        timeArray[position] = timeValue;
        if (voltageArray[position] < voltageMinimum) voltageMinimum = voltageArray[position];
        if (voltageArray[position] > voltageMaximum) voltageMaximum = voltageArray[position];
    }

    public double[] getVoltage() {
        return voltageArray;
    }

    public double getVoltageOnPosition(int position) {
        return voltageArray[position];
    }

    public void setVoltageOnPosition(double value, int position) {
        voltageArray[position] = value;
        if (voltageArray[position] < voltageMinimum) voltageMinimum = voltageArray[position];
        if (voltageArray[position] > voltageMaximum) voltageMaximum = voltageArray[position];
    }

    public String getName() { return name; }

    public double[] getTime() {
        return timeArray;
    }

    public double getTimeOnPosition(int position) {
        return timeArray[position];
    }

    public void setTimeOnPosition(int position, double value) {
        timeArray[position] = value;
    }

    public String getVoltageUnit() {
        return voltageUnit;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public int getDataCount() {
        return dataCount;
    }

    public int getIndexOfTimeValue(double timeValue) {
        double distance = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < dataCount; i++) {
            double currentDistance = Math.abs(timeValue - timeArray[i]);
            if (currentDistance < distance) {
                distance = currentDistance;
                index = i;
            }
        }

        return index;
    }

    public int getSamplingFrequency() {
        BigDecimal dT = BigDecimal.valueOf(timeArray[0])
                .abs()
                .setScale(10, RoundingMode.HALF_UP)
                .subtract(BigDecimal
                        .valueOf(timeArray[1])
                        .abs()
                        .setScale(10, RoundingMode.HALF_UP))
                .abs()
                .divide(UnitsHelper.getInvertedTimeUnitConstant(timeUnit), 10, RoundingMode.HALF_UP);
        dT = BigDecimal.ONE.divide(dT, 10, RoundingMode.HALF_UP);
        return dT.intValue();
    }
    
    public double getMaximalVoltage() {
        return voltageMaximum;
    }
    
    public double getMinimalVoltage() {
        return voltageMinimum;
    }
}
