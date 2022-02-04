package muni.cotemplate.module;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import com.aparapi.internal.opencl.OpenCLPlatform;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPUCorrelationComputer implements Runnable {
    //input
    private final float[] voltage;
    private final int[] froms;
    private final int[] tos;
    private final int characterCount;
    private final int segmentWidth;
    private final int endingIndex;

    Character currentCharacter;

    // Output
    private final HashMap<Character, double[]> correlations;

    public GPUCorrelationComputer(
            double[] voltage,
            HashMap<Character, double[]> correlations,
            Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals,
            int characterCount,
            int segmentWidth,
            int endingIndex) {
        this.voltage = getFloatArray(voltage);
        this.correlations = correlations;
        this.characterCount = characterCount;
        this.segmentWidth = segmentWidth;
        this.endingIndex = endingIndex;
        this.froms = getIntArray(characterIntervals, p -> p.getKey());
        this.tos = getIntArray(characterIntervals, p -> p.getValue());
        this.currentCharacter = characterIntervals.getKey();
    }

    public void run() {
        int correlationsLength = correlations.get(currentCharacter).length;
        final float[] correlationsForCharacter = new float[correlationsLength];
        final int intervalsLengthLocal = froms.length;
        final float[] voltageLocal = voltage;
        final int[] fromsLocal = froms;
        final int[] tosLocal = tos;
        final int characterCountLocal = characterCount;
        final int segmentWidthLocal = segmentWidth;
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int windowIndex = getGlobalId();
                float correlationSums = 0f;
                for (int intervalIndex = 0; intervalIndex < intervalsLengthLocal; intervalIndex++) {
                    float segmentCorrelation = correlationCoefficientStable(windowIndex + fromsLocal[intervalIndex],windowIndex + tosLocal[intervalIndex], windowIndex);
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
                    for (int segmentIndex = 0; segmentIndex < intervalsLengthLocal; segmentIndex++) {
                        segmentSum = segmentSum + voltageLocal[fromsLocal[segmentIndex] + (intervalIndex - windowIndex)];
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

        System.out.println("com.aparapi.examples.info.Main");
        List<OpenCLPlatform> platforms = (new OpenCLPlatform()).getOpenCLPlatforms();
        System.out.println("Machine contains " + platforms.size() + " OpenCL platforms");
        int platformc = 0;
        for (OpenCLPlatform platform : platforms) {
            System.out.println("Platform " + platformc + "{");
            System.out.println("   Name    : \"" + platform.getName() + "\"");
            System.out.println("   Vendor  : \"" + platform.getVendor() + "\"");
            System.out.println("   Version : \"" + platform.getVersion() + "\"");
            List<OpenCLDevice> devices = platform.getOpenCLDevices();
            System.out.println("   Platform contains " + devices.size() + " OpenCL devices");
            int devicec = 0;
            for (OpenCLDevice device : devices) {
                System.out.println("   Device " + devicec + "{");
                System.out.println("       Type                  : " + device.getType());
                System.out.println("       GlobalMemSize         : " + device.getGlobalMemSize());
                System.out.println("       LocalMemSize          : " + device.getLocalMemSize());
                System.out.println("       MaxComputeUnits       : " + device.getMaxComputeUnits());
                System.out.println("       MaxWorkGroupSizes     : " + device.getMaxWorkGroupSize());
                System.out.println("       MaxWorkItemDimensions : " + device.getMaxWorkItemDimensions());
                System.out.println("   }");
                devicec++;
            }
            System.out.println("}");
            platformc++;
        }

        KernelPreferences preferences = KernelManager.instance().getDefaultPreferences();
        System.out.println("\nDevices in preferred order:\n");

        for (Device device : preferences.getPreferredDevices(null)) {
            System.out.println(device);
            System.out.println();
        }

        Range range = Range.create(endingIndex);
        System.out.println("Max work group size: " + range.getMaxWorkGroupSize());
        kernel.execute(range);

        // resulting correlations copy to corr array for character :'(
        double[] doubleCorrelations = correlations.get(currentCharacter);
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

    private int[] getIntArray(Map.Entry<Character, List<Pair<Integer, Integer>>> characterIntervals, PairSelector ps) {
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
