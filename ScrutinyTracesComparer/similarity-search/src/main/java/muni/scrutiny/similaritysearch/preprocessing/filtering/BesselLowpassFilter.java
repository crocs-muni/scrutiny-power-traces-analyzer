package muni.scrutiny.similaritysearch.preprocessing.filtering;

import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;
import uk.me.berndporr.iirj.Bessel;
import uk.me.berndporr.iirj.ChebyshevI;

public class BesselLowpassFilter  implements Preprocessor {
    public static final int order = 1;
    public static final double defaultCutoffFrequency = 10000;
    private double cutoffFrequency;

    public BesselLowpassFilter(Double cutoffFrequency) {
        this.cutoffFrequency = cutoffFrequency != null ? cutoffFrequency : defaultCutoffFrequency;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        Bessel bessel = new Bessel();
        bessel.lowPass(order, traceToPreprocess.getSamplingFrequency(), cutoffFrequency);
        double[] voltageArray = traceToPreprocess.getVoltageCopy();
        double firstData = voltageArray[0];
        for (int i = 0; i < voltageArray.length; i++) {
            voltageArray[i] = bessel.filter(voltageArray[i] - firstData) + firstData;
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
