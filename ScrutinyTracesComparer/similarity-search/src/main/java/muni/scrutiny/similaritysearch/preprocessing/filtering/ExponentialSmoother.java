package muni.scrutiny.similaritysearch.preprocessing.filtering;

import edu.mines.jtk.dsp.RecursiveExponentialFilter;
import muni.scrutiny.similaritysearch.preprocessing.base.Preprocessor;
import muni.scrutiny.traces.models.Trace;

public class ExponentialSmoother implements Preprocessor {
    public static final double defaultAlpha = 1;
    private double alpha;

    public ExponentialSmoother(Double alpha) {
        this.alpha = alpha != null ? alpha : defaultAlpha;
    }

    @Override
    public Trace preprocess(Trace traceToPreprocess) {
        RecursiveExponentialFilter ref = new RecursiveExponentialFilter(alpha);
        float[] floatVoltageCopy = traceToPreprocess.getFloatVoltageCopy();
        ref.apply(floatVoltageCopy, floatVoltageCopy);
        double[] doubleVoltageCopy = new double[floatVoltageCopy.length];
        for (int i = 0; i < floatVoltageCopy.length; i++) {
            doubleVoltageCopy[i] = floatVoltageCopy[i];
        }

        return new Trace(
                traceToPreprocess.getName(),
                doubleVoltageCopy.length,
                traceToPreprocess.getVoltageUnit(),
                traceToPreprocess.getTimeUnit(),
                doubleVoltageCopy,
                traceToPreprocess.getSamplingFrequency());
    }
}
