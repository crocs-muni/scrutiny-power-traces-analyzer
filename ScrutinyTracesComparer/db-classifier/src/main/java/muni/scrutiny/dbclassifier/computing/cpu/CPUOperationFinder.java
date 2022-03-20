package muni.scrutiny.dbclassifier.computing.cpu;

import muni.scrutiny.dbclassifier.computing.OperationFinder;
import muni.scrutiny.dbclassifier.computing.models.OperationFinderResult;
import muni.scrutiny.traces.models.Trace;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CPUOperationFinder implements OperationFinder {
    @Override
    public OperationFinderResult findOperations(Trace unknownTrace, Trace operationTrace, int takeNth) {
        if (unknownTrace.getDataCount() < operationTrace.getDataCount()) {
            throw new IllegalArgumentException("Parameter biggerArray is smaller than smallerArray.");
        }

        int cores = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
        int jump = unknownTrace.getDataCount() / cores;
        double[] unknownTraceArray = unknownTrace.getVoltage();
        double[] operationTraceArray = operationTrace.getVoltage();
        int innerWindowLength = operationTraceArray.length;
        int endingIndex = unknownTraceArray.length - innerWindowLength;
        double[] distances = initDistances(unknownTrace.getDataCount());

        for (int index = 0; index < endingIndex; index += jump) {
            executor.execute(new EuclideanSlidingWindowTask(
                    unknownTraceArray,
                    operationTraceArray,
                    innerWindowLength,
                    distances,
                    index,
                    Math.min(index + jump, endingIndex),
                    takeNth));
        }

        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OperationFinderResult ofr = new OperationFinderResult();
        ofr.distances = postprocess(distances);
        return ofr;
    }

    private double[] initDistances(int size) {
        double[] matchIndexes = new double[size];
        Arrays.fill(matchIndexes, -1);
        return matchIndexes;
    }

    private double[] postprocess(double[] distances) {
        double previousValidNumber = Arrays.stream(distances).max().orElse(Double.MAX_VALUE);
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < 0) {
                distances[i] = previousValidNumber;
            }
        }

        return distances;
    }
}
