package muni.scrutiny.dbclassifier.computing.gpu;

import com.aparapi.Kernel;

public class EuclideanSlidingWindowKernel extends Kernel {
    private final float[] unknownTraceFloatArray;
    private final float[] operationTraceFloatArray;
    private final int innerWindowLength;
    private final float[] distances;
    private final int takeNth;

    public EuclideanSlidingWindowKernel(
            float[] unknownTraceFloatArray,
            float[] operationTraceFloatArray,
            int innerWindowLength,
            float[] distances,
            int takeNth) {
        this.unknownTraceFloatArray = unknownTraceFloatArray;
        this.operationTraceFloatArray = operationTraceFloatArray;
        this.innerWindowLength = innerWindowLength;
        this.distances = distances;
        this.takeNth = takeNth;
    }

    @Override
    public void run() {
        int windowIndex = getGlobalId(0)*takeNth;
        float sum = 0f;
        for (int i = 0; i < innerWindowLength; i++) {
            sum += (unknownTraceFloatArray[windowIndex + i] - operationTraceFloatArray[i])
                    * (unknownTraceFloatArray[windowIndex + i] - operationTraceFloatArray[i]);
        }

        float result = sqrt(sum);
        distances[windowIndex] = result;
    }
}
