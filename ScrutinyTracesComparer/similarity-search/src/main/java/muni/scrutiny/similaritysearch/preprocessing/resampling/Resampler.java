package muni.scrutiny.similaritysearch.preprocessing.resampling;

import muni.scrutiny.traces.models.Trace;

public interface Resampler {
    Trace resample();
}
