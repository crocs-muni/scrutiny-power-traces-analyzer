package muni.cotemplate.module;

import com.aparapi.Kernel;
import com.aparapi.Range;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPUCorrelationComputer implements Runnable {
    private final float[] voltage;
    private final HashMap<Character, double[]> correlations;
    private final Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals;
    private final int characterCount;
    private final int segmentWidth;
    private final int endingIndex;

    public GPUCorrelationComputer(
            double[] voltage,
            HashMap<Character, double[]> correlations,
            Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals,
            int characterCount,
            int segmentWidth,
            int endingIndex) {
        this.voltage = getFloatArray(voltage);
        this.correlations = correlations;
        this.characterIntervals = characterIntervals;
        this.characterCount = characterCount;
        this.segmentWidth = segmentWidth;
        this.endingIndex = endingIndex;
    }

    public void run() {
        int correlationsLength = correlations.get(characterIntervals.getKey()).length;
        final float[] correlationsForCharacter = new float[correlationsLength];
        final int[] froms = getIntArray(p -> p.getKey());
        final int[] tos = getIntArray(p -> p.getValue());
        final int intervalsLength = froms.length;
        final float[] voltageLocal = voltage;
        final int characterCountLocal = characterCount;
        final int segmentWidthLocal = segmentWidth;
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int windowIndex = getGlobalId();
                float correlationSums = 0f;
                for (int intervalIndex = 0; intervalIndex < intervalsLength; intervalIndex++) {
                    float segmentCorrelation = correlationCoefficientStable(windowIndex + froms[intervalIndex],windowIndex + tos[intervalIndex], windowIndex);
                    correlationSums += segmentCorrelation;
                }

                correlationsForCharacter[windowIndex] = correlationSums / characterCountLocal;
            }

            private float correlationCoefficientStable(final int intervalFrom, final int intervalTo, final int windowIndex) {
                float sumX = 0;
                float sumY = 0;
                float sumXY = 0;
                float squareSumX = 0;
                float squareSumY = 0;
                for (int intervalIndex = intervalFrom; intervalIndex < intervalTo; intervalIndex++) {
                    float segmentSum = 0f;
                    for (int segmentIndex = 0; segmentIndex < intervalsLength; segmentIndex++) {
                        segmentSum = segmentSum + voltageLocal[froms[segmentIndex] + (intervalIndex - windowIndex)];
                    }

                    float segmentAverageOnIndex = segmentSum / characterCountLocal;
                    sumX = sumX + voltageLocal[intervalIndex];
                    sumY = sumY + segmentAverageOnIndex;
                    sumXY = sumXY + voltageLocal[intervalIndex] * segmentAverageOnIndex;
                    squareSumX = squareSumX + voltageLocal[intervalIndex] * voltageLocal[intervalIndex];
                    squareSumY = squareSumY + segmentAverageOnIndex * segmentAverageOnIndex;
                }

                float corr = (segmentWidthLocal * sumXY - sumX * sumY) / sqrt(((segmentWidthLocal * squareSumX - sumX * sumX)*(segmentWidthLocal * squareSumY - sumY * sumY))+0.00001f);
                return corr;
            }
        };

        Range range = Range.create(endingIndex);
        kernel.execute(range);

        // resulting correlations copy to corr array for character :'(
        double[] doubleCorrelations = correlations.get(characterIntervals.getKey());
        for (int i = 0; i < correlationsForCharacter.length; i++) {
            doubleCorrelations[i] = correlationsForCharacter[i];
        }

        kernel.dispose();
    }

    private float[] getFloatArray(double[] doubleVoltage) {
        float[] floatVoltage = new float[doubleVoltage.length];
        for (int i = 0; i < doubleVoltage.length; i++) {
            floatVoltage[i] = (float)doubleVoltage[i];
        }

        return floatVoltage;
    }

    private int[] getIntArray(PairSelector ps) {
        int[] ints = new int[characterIntervals.getValue().size()];
        for (int i = 0; i < characterIntervals.getValue().size(); i++) {
            ints[i] = ps.pairSelector(characterIntervals.getValue().get(i));
        }

        return ints;
    }

    protected interface PairSelector {
        int pairSelector(Pair<Integer, Integer> pair);
    }
}
