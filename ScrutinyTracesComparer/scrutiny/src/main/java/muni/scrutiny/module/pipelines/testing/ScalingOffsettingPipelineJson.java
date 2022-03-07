package muni.scrutiny.module.pipelines.testing;

import com.google.gson.annotations.SerializedName;

public class ScalingOffsettingPipelineJson {
    @SerializedName("scale")
    public double scale;

    @SerializedName("offset")
    public double offset;
}
