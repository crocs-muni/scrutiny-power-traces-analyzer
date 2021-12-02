package muni.scrutiny.module.configurations.reference;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class ReferenceCardConfig {
    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("operations")
    public List<ReferenceCardOperation> operations;
}
