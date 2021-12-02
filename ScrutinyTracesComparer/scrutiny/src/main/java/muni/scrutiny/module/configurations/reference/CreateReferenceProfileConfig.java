package muni.scrutiny.module.configurations.reference;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.util.List;

public class CreateReferenceProfileConfig {
    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("pipelines")
    public List<String> pipelines;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("operations")
    public List<CreateReferenceProfileOperation> operations;
}
