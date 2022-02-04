package muni.scrutiny.module.pipelines.testing;

import com.google.gson.annotations.SerializedName;

public class ResamplingPipelineJson {
    @SerializedName("sampling_frequency")
    public int sampingFrequency;

    @SerializedName("interval_radius")
    public int intervalRadius;
}
