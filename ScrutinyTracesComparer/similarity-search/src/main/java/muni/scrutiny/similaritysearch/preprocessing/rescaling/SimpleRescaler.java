package muni.scrutiny.similaritysearch.preprocessing.rescaling;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;

import java.util.Optional;

public class SimpleRescaler implements Preprocessor {
    private static final double defaultIntervalCoefficient = 0.1;

    private final double referenceMinimum;
    private final double referenceMaximum;
    private final Double scale;
    private final double intervalCoefficient;

    public SimpleRescaler(
            double referenceMinimum,
            double referenceMaximum,
            Double scale,
            Double intervalCoefficient) {
        this.referenceMinimum = referenceMinimum;
        this.referenceMaximum = referenceMaximum;
        this.scale = scale;
        this.intervalCoefficient = intervalCoefficient != null ? intervalCoefficient : defaultIntervalCoefficient;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        double[] voltageArray = traceToPreprocess.getVoltageCopy();

        if (scale != null) {
            for (int i = 0; i < voltageArray.length; i++) {
                voltageArray[i] /= scale;
            }

            return traceToPreprocess;
        }

        double newMinimalVoltage = traceToPreprocess.getMinimalVoltage();
        double newMaximalVoltage = traceToPreprocess.getMaximalVoltage();
        double diffMinimal = referenceMinimum/newMinimalVoltage;
        double diffMaximal = referenceMaximum/newMaximalVoltage;
        double averageDiff = (diffMaximal + diffMinimal) / 2;
        // This if represents if we can speak about offset inconsistency
        if (((1-intervalCoefficient) * diffMinimal <= diffMaximal
                && diffMaximal <= (1 + intervalCoefficient) * diffMinimal)
                || ((1-intervalCoefficient) * diffMaximal <= diffMinimal
                && diffMinimal <= (1 + intervalCoefficient) * diffMaximal)) {
            // This if represents if it is worthy doing normalization
            if (((1 - intervalCoefficient) * newMaximalVoltage  <= newMaximalVoltage/diffMaximal
                    && newMaximalVoltage/diffMaximal <= (1 + intervalCoefficient) * newMaximalVoltage)
                    || ((1 - intervalCoefficient) * newMinimalVoltage  <= newMinimalVoltage + diffMinimal
                    && newMinimalVoltage + diffMinimal <= (1 + intervalCoefficient) * newMinimalVoltage)) {
                for (int i = 0; i < voltageArray.length; i++) {
                    voltageArray[i] /= averageDiff;
                }
            }
        }

        return new Trace(
                traceToPreprocess.getName(),
                voltageArray.length,
                traceToPreprocess.getVoltageUnit(),
                traceToPreprocess.getTimeUnit(),
                voltageArray,
                traceToPreprocess.getSamplingFrequency());
    }
}
