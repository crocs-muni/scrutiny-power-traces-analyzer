package muni.scrutiny.similaritysearch.preprocessing.filtering;
import java.util.Optional;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;
import uk.me.berndporr.iirj.Butterworth;

/**
 * Class that represents low-pass filter.
 * It is used to filter voltage array of the trace.
 *
 * @author Martin Podhora
 */
public class LowpassFilter implements Preprocessor {
    public static final int order = 1;
    public static final double defaultCutoffFrequency = 20000;
    private double cutoffFrequency;

    public LowpassFilter(Double cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency != null ? cutoffFrequency : defaultCutoffFrequency;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        Butterworth butterworth = new Butterworth();
        butterworth.lowPass(order, traceToPreprocess.getSamplingFrequency(), cutoffFrequency);
        double[] voltageArray = traceToPreprocess.getVoltage();
        double newMinimum = Double.MAX_VALUE;
        double newMaximum = Double.MIN_VALUE;
        for (int i = 0; i < voltageArray.length; i++) {
            voltageArray[i] = butterworth.filter(voltageArray[i]);

            if (voltageArray[i] > newMaximum) {
                newMaximum = voltageArray[i];
            }

            if(voltageArray[i] < newMinimum) {
                newMinimum = voltageArray[i];
            }
        }

        return new Trace(
                traceToPreprocess.getName(),
                traceToPreprocess.getVoltageUnit(),
                traceToPreprocess.getTimeUnit(),
                traceToPreprocess.getDataCount(),
                voltageArray,
                null,
                newMaximum,
                newMinimum);
    }
}
