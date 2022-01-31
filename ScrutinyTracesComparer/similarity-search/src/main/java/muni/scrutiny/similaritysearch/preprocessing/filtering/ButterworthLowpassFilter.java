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
public class ButterworthLowpassFilter implements Preprocessor {
    public static final int order = 1;
    public static final double defaultCutoffFrequency = 20000;
    private double cutoffFrequency;

    public ButterworthLowpassFilter(Double cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency != null ? cutoffFrequency : defaultCutoffFrequency;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        Butterworth butterworth = new Butterworth();
        butterworth.lowPass(order, traceToPreprocess.getSamplingFrequency(), cutoffFrequency);
        double[] voltageArray = traceToPreprocess.getVoltage();
        double firstData = voltageArray[0];
        for (int i = 0; i < voltageArray.length; i++) {
            voltageArray[i] = butterworth.filter(voltageArray[i] - firstData) + firstData;
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
