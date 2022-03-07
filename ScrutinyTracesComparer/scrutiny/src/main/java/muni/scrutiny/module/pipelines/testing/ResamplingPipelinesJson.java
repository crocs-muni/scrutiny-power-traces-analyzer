package muni.scrutiny.module.pipelines.testing;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResamplingPipelinesJson {
    @SerializedName("before")
    public ResamplingPipelineJson before;

    @SerializedName("after")
    public ResamplingPipelineJson after;
}
