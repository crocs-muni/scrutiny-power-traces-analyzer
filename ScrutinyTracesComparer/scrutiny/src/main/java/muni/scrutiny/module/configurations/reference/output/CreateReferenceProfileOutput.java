package muni.scrutiny.module.configurations.reference.output;

import com.google.gson.annotations.SerializedName;
import muni.scrutiny.module.configurations.module.ScrutinyModule;
import muni.scrutiny.module.pipelines.base.CustomPipelineParameters;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateReferenceProfileOutput {
    @SerializedName("card_code")
    public String cardCode;

    @SerializedName("custom_parameters")
    public CustomPipelineParameters customParameters;

    @SerializedName("operations")
    public List<CreateReferenceProfileOutputOperation> operations;

    @SerializedName("created_date")
    public LocalDateTime createdDate;

    @SerializedName("created_by")
    public String createdBy;

    @SerializedName("additional_info")
    public String additionalInfo;
}
