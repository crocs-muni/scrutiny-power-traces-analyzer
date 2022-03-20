package muni.scrutiny.dbclassifier.computing.cpu;

import java.util.concurrent.atomic.AtomicInteger;

public class EuclideanSlidingWindowTask implements Runnable {
    private final double[] unknownTraceArray;
    private final double[] operationTraceArray;
    private final int innerWindowLength;
    private int takeNth;
    private double[] distances;
    private final int from;
    private final int to;

    public EuclideanSlidingWindowTask(
            double[] unknownTraceArray,
            double[] operationTraceArray,
            int innerWindowLength,
            double[] distances,
            int from,
            int to,
            int takeNth) {
        this.unknownTraceArray = unknownTraceArray;
        this.operationTraceArray = operationTraceArray;
        this.innerWindowLength = innerWindowLength;
        this.takeNth = takeNth;
        this.distances = distances;
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        for (int windowIndex = from; windowIndex < to; windowIndex += takeNth) {
            double sum = 0;
            for (int i = 0; i < innerWindowLength; i++) {
                sum += (unknownTraceArray[windowIndex + i] - operationTraceArray[i])
                        * (unknownTraceArray[windowIndex + i] - operationTraceArray[i]);
            }

            double result = Math.sqrt(sum);
            distances[windowIndex] = result;
        }
    }
}
