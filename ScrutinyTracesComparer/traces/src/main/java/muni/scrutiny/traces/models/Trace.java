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

    private double[] timeArray;

    private final int dataCount;

    private final String voltageUnit;

    private final String timeUnit;
    
    private double voltageMaximum;
    
    private double voltageMinimum;

    private int samplingFrequency;

    public Trace(
            String name,
            int dataCount,
            String voltageUnit,
            String timeUnit,
            int samplingFrequency,
            boolean initTimeArray) {
        this.name = name;
        this.voltageUnit = voltageUnit;
        this.timeUnit = timeUnit;
        this.dataCount = dataCount;
        this.voltageArray = new double[dataCount];
        this.samplingFrequency = samplingFrequency;
        if (initTimeArray) {
            this.timeArray = new double[dataCount];
        } else {
            this.timeArray = null;
        }

        this.voltageMaximum = Double.NEGATIVE_INFINITY;
        this.voltageMinimum = Double.POSITIVE_INFINITY;
    }

    public Trace(
            String name,
            int dataCount,
            String voltageUnit,
            String timeUnit,
            double[] voltageArray,
            int samplingFrequency) {
        this.name = name;
        this.voltageUnit = voltageUnit;
        this.timeUnit = timeUnit;
        this.dataCount = dataCount;
        this.voltageArray = voltageArray;
        this.samplingFrequency = samplingFrequency;
        this.timeArray = null;
        this.voltageMaximum = Double.MIN_VALUE;
        this.voltageMinimum = Double.MAX_VALUE;
        for (double v : voltageArray) {
            if (this.voltageMaximum < v) {
                this.voltageMaximum = v;
            }

            if (this.voltageMinimum > v) {
                this.voltageMinimum = v;
            }
        }
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

    public Trace(Trace t) {
        this.name = t.getName();
        this.voltageUnit = t.getVoltageUnit();
        this.timeUnit = t.getTimeUnit();
        this.dataCount = t.getDataCount();
        this.voltageArray = t.getVoltage().clone();
        this.timeArray = t.getTime(false).clone();
        this.voltageMaximum = t.getMaximalVoltage();
        this.voltageMinimum = t.getMinimalVoltage();
        this.samplingFrequency = t.getSamplingFrequency();
    }

    public void addData(double voltageValue, double timeValue, int position) {
        voltageArray[position] = voltageValue;
        if (timeArray != null) {
            timeArray[position] = timeValue;
        }
        if (voltageArray[position] < voltageMinimum) voltageMinimum = voltageArray[position];
        if (voltageArray[position] > voltageMaximum) voltageMaximum = voltageArray[position];
    }

    public double[] getVoltage() {
        return voltageArray;
    }

    public String getName() { return name; }

    public String getDisplayName() { return name.replace(".csv", ""); }

    public double[] getTime(boolean recalculate)
    {
        if (timeArray == null || recalculate) {
            double samplingFreqTimeUnit = getSamplingFrequencyTimeUnit();
            double nextTime = 0d;
            double[] time = new double[getDataCount()];
            for (int i = 0; i < getDataCount(); i++) {
                time[i] = nextTime;
                nextTime += samplingFreqTimeUnit;
            }

            this.timeArray = time;
            return time;
        }

        return timeArray;
    }

    public double[] getTime(boolean recalculate, int indexOffset)
    {
        if (timeArray == null || recalculate) {
            double samplingFreqTimeUnit = getSamplingFrequencyTimeUnit();
            double nextTime = indexOffset * samplingFreqTimeUnit;
            double[] time = new double[getDataCount()];
            for (int i = 0; i < getDataCount(); i++) {
                time[i] = nextTime;
                nextTime += samplingFreqTimeUnit;
            }

            this.timeArray = time;
            return time;
        }

        return timeArray;
    }

    public double getVoltageOnIndex(int index)
    {
        return voltageArray[index];
    }

    public double getTimeOnIndex(int index)
    {
        return timeArray[index];
    }

    public double getExecutionTime() {
        return getSamplingFrequencyTimeUnit() * getDataCount();
    }

    public String getVoltageUnit() {
        return voltageUnit;
    }

    public String getDisplayVoltageUnit() {

        return "U" + voltageUnit;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public String getDisplayTimeUnit() {
        return "t" + timeUnit;
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
        if (samplingFrequency > 0) {
            return samplingFrequency;
        }

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

        samplingFrequency = dT.intValue();
        return samplingFrequency;
    }

    public double getSamplingFrequencyTimeUnit() {
        if (samplingFrequency > 0) {
            return BigDecimal
                    .ONE
                    .divide(BigDecimal.valueOf(samplingFrequency), 10, RoundingMode.HALF_UP)
                    .multiply(UnitsHelper.getInvertedTimeUnitConstant(timeUnit))
                    .doubleValue();
        }

        BigDecimal dT = BigDecimal.valueOf(timeArray[0])
                .abs()
                .setScale(10, RoundingMode.HALF_UP)
                .subtract(BigDecimal
                        .valueOf(timeArray[1])
                        .abs()
                        .setScale(10, RoundingMode.HALF_UP))
                .abs()
                .divide(UnitsHelper.getInvertedTimeUnitConstant(timeUnit), 10, RoundingMode.HALF_UP);
        return dT.multiply(UnitsHelper.getInvertedTimeUnitConstant(timeUnit)).doubleValue();
    }
    
    public double getMaximalVoltage() {
        return voltageMaximum;
    }
    
    public double getMinimalVoltage() {
        return voltageMinimum;
    }

    public static int getSamplingFrequency(double t0, double t1, String timeUnit) {
        BigDecimal dT = BigDecimal.valueOf(t0)
                .abs()
                .setScale(10, RoundingMode.HALF_UP)
                .subtract(BigDecimal
                        .valueOf(t1)
                        .abs()
                        .setScale(10, RoundingMode.HALF_UP))
                .abs()
                .divide(UnitsHelper.getInvertedTimeUnitConstant(timeUnit), 10, RoundingMode.HALF_UP);
        dT = BigDecimal.ONE.divide(dT, 10, RoundingMode.HALF_UP);

        return dT.intValue();
    }
}
