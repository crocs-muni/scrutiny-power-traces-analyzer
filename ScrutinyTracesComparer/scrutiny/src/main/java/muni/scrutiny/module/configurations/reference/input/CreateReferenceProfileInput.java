package muni.scrutiny.module.configurations.reference.input;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.time.LocalDateTime;
import java.util.List;

public class CreateReferenceProfileInput {
    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("pipelines")
    public List<String> pipelines;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("operations")
    public List<CreateReferenceProfileInputOperation> operations;

    @SerializedName("created_by")
    public String createdBy;

    @SerializedName("additional_info")
    public String additionalInfo;
}
