package muni.scrutiny.module.configurations.input.reference;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class ReferenceCardConfig {
    @SerializedName("pipeline")
    public String pipeline;

    @SerializedName("parameters")
    public CustomPipelineParameters parameters;

    @SerializedName("traces")
    public List<ReferenceCardConfigTrace> traces;
}
