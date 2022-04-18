package muni.scrutiny.dbclassifier.computing.gpu;

import com.aparapi.Kernel;
import com.aparapi.Range;
import muni.scrutiny.dbclassifier.computing.OperationFinder;
import muni.scrutiny.dbclassifier.computing.models.OperationFinderResult;
import muni.scrutiny.traces.models.Trace;

import java.util.Arrays;
import java.util.List;

public class GPUOperationFinder implements OperationFinder {
    private static final float initialNumber = -1f;

    @Override
    public OperationFinderResult findOperations(Trace unknownTrace, Trace operationTrace, int takeNth) {
        if (unknownTrace.getDataCount() < operationTrace.getDataCount()) {
            throw new IllegalArgumentException("Parameter biggerArray is smaller than smallerArray.");
        }

        float[] unknownTraceFloatArray = unknownTrace.getFloatVoltageCopy();
        float[] operationTraceFloatArray = operationTrace.getFloatVoltageCopy();
        int innerWindowLength = operationTraceFloatArray.length;
        float[] distances = initDistances(unknownTrace.getDataCount());

        Kernel kernel = new EuclideanSlidingWindowKernel(
                unknownTraceFloatArray,
                operationTraceFloatArray,
                innerWindowLength,
                distances,
                takeNth);

        int windowSlidesCount = (unknownTrace.getDataCount() - operationTrace.getDataCount()) / takeNth;
        Range range = Range.create(windowSlidesCount);
        kernel.execute(range);

        OperationFinderResult ofr = new OperationFinderResult();
        ofr.distances = toDouble(distances);
        return ofr;
    }

    private float[] initDistances(int size) {
        float[] distances = new float[size];
        Arrays.fill(distances, initialNumber);
        return distances;
    }

    private double[] toDouble(float[] floatDistances) {
        float previousValidNumber = getFirstNoninitialNumber(floatDistances, initialNumber);
        double[] distances = new double[floatDistances.length];
        for (int i = 0; i < floatDistances.length; i++) {
            if (distances[i] < 0) {
                distances[i] = previousValidNumber;
            }

            previousValidNumber = floatDistances[i];
            distances[i] = floatDistances[i];
        }

        return distances;
    }

    private float getFirstNoninitialNumber(float[] floatDistances, float initialNumber) {
        for (int i = 0; i < floatDistances.length; i++) {
            if (floatDistances[i] > initialNumber) {
                return floatDistances[i];
            }
        }

        return 0f;
    }
}